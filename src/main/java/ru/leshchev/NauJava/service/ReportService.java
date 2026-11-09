package ru.leshchev.NauJava.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;
import ru.leshchev.NauJava.dal.entity.Report;
import ru.leshchev.NauJava.dal.entity.ReportStatus;
import ru.leshchev.NauJava.dal.repository.ReportRepository;
import ru.leshchev.NauJava.dal.repository.UserRepository;
import ru.leshchev.NauJava.web.dto.ProjectResponse;
import ru.leshchev.NauJava.web.error.ResourceNotFoundException;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.Supplier;

@Service
@Slf4j
public class ReportService {
    private final ReportRepository reports;
    private final UserRepository users;
    private final ProjectQueryService projects;
    private final Executor executor;

    public ReportService(ReportRepository reports, UserRepository users, ProjectQueryService projects,
                         @Qualifier("applicationTaskExecutor") Executor executor) {
        this.reports = reports;
        this.users = users;
        this.projects = projects;
        this.executor = executor;
    }

    @Transactional
    public Long createReport() {
        return reports.save(new Report()).getId();
    }

    public Report getReport(Long id) {
        return reports.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report " + id + " not found"));
    }

    public CompletableFuture<Void> generateAsync(Long id) {
        try {
            return CompletableFuture.runAsync(() -> {
                try {
                    generate(id);
                } catch (RuntimeException exception) {
                    markFailed(id, exception);
                    throw exception;
                }
            }, executor);
        } catch (RejectedExecutionException exception) {
            markFailed(id, exception);
            return CompletableFuture.failedFuture(exception);
        }
    }

    private void markFailed(Long id, RuntimeException failure) {
        log.error("Report {} generation failed", id, failure);
        reports.save(getReport(id).setStatus(ReportStatus.ERROR).setContent(null));
    }

    private void generate(Long id) {
        long startedAt = System.nanoTime();
        var userCount = new FutureTask<>(() -> measure(users::countByUsernameIsNotNullAndPasswordIsNotNull));
        var projectList = new FutureTask<>(() -> measure(projects::findAll));
        var usersThread = new Thread(userCount, "report-" + id + "-users");
        var projectsThread = new Thread(projectList, "report-" + id + "-projects");

        usersThread.start();
        projectsThread.start();
        try {
            usersThread.join();
            projectsThread.join();
            String content = render(userCount.get(), projectList.get(), startedAt);
            reports.save(getReport(id).setContent(content).setStatus(ReportStatus.COMPLETED));
        } catch (InterruptedException exception) {
            usersThread.interrupt();
            projectsThread.interrupt();
            Thread.currentThread().interrupt();
            throw new CompletionException(exception);
        } catch (ExecutionException exception) {
            throw new CompletionException(exception.getCause());
        }
    }

    private static <T> Measured<T> measure(Supplier<T> operation) {
        long startedAt = System.nanoTime();
        T value = operation.get();
        return new Measured<>(value, elapsedMillis(startedAt));
    }

    private static double elapsedMillis(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000.0;
    }

    private static String render(Measured<Long> userCount, Measured<List<ProjectResponse>> projectList,
                                 long startedAt) {
        var rows = new StringBuilder();
        for (ProjectResponse project : projectList.value()) {
            rows.append("<tr><td>").append(project.id())
                    .append("</td><td>").append(HtmlUtils.htmlEscape(project.name(), "UTF-8"))
                    .append("</td><td>").append(project.status()).append("</td></tr>");
        }
        if (rows.isEmpty()) {
            rows.append("<tr><td colspan='3'>Нет проектов</td></tr>");
        }

        String page = String.format(Locale.ROOT, """
                <!DOCTYPE html>
                <html lang="ru">
                <head>
                    <meta charset="UTF-8">
                    <title>Отчёт по проектам</title>
                </head>
                <body>
                    <h1>Отчёт по проектам</h1>
                    <p>Зарегистрированных пользователей: %d</p>
                    <p>Время подсчёта пользователей: %.3f мс</p>
                    <p>Время получения списка проектов: %.3f мс</p>
                    <table>
                        <caption>Проекты</caption>
                        <thead><tr><th>ID</th><th>Название</th><th>Статус</th></tr></thead>
                        <tbody>%s</tbody>
                    </table>
                """, userCount.value(), userCount.millis(), projectList.millis(), rows);
        return page + String.format(Locale.ROOT, """
                    <p>Общее время формирования отчёта: %.3f мс</p>
                </body>
                </html>
                """, elapsedMillis(startedAt));
    }

    private record Measured<T>(T value, double millis) {}
}
