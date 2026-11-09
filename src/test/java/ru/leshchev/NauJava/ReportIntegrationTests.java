package ru.leshchev.NauJava;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import ru.leshchev.NauJava.dal.entity.Project;
import ru.leshchev.NauJava.dal.entity.ReportStatus;
import ru.leshchev.NauJava.dal.entity.User;
import ru.leshchev.NauJava.dal.repository.ProjectRepository;
import ru.leshchev.NauJava.dal.repository.ReportRepository;
import ru.leshchev.NauJava.dal.repository.UserRepository;
import ru.leshchev.NauJava.service.ProjectQueryService;

import java.net.URI;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Timeout(20)
class ReportIntegrationTests {
    private static final String REPORTS = "/custom/reports";
    @LocalServerPort int port;
    @Autowired UserRepository users;
    @Autowired ProjectRepository projects;
    @Autowired ReportRepository reports;
    @Autowired PasswordEncoder passwordEncoder;
    @MockitoSpyBean ProjectQueryService projectQueries;
    private HttpTestClient client;

    @BeforeEach
    void logIn() throws Exception {
        users.save(new User().setName("Report").setLastName("User").setEmail("report@example.org")
                .setUsername("report-user").setPassword(passwordEncoder.encode("test-password")));
        client = new HttpTestClient(port);
        assertThat(client.login("report-user", "test-password").statusCode()).isEqualTo(302);
        client.csrf("/projects");
    }

    @AfterEach
    void cleanDatabase() {
        reports.deleteAll();
        projects.deleteAll();
        users.deleteAll();
    }

    @Test
    void returnsIdBeforeGenerationCompletesAndPersistsHtml() throws Exception {
        users.save(new User().setName("Project").setLastName("Participant").setEmail("participant@example.org"));
        projects.save(new Project().setName("Проект <script>alert(1)</script>"));

        var started = new CountDownLatch(1);
        var release = new CountDownLatch(1);
        doAnswer(invocation -> {
            assertThat(Thread.currentThread().getName()).endsWith("-projects");
            started.countDown();
            assertThat(release.await(10, TimeUnit.SECONDS)).isTrue();
            return invocation.callRealMethod();
        }).when(projectQueries).findAll();

        String path;
        try {
            path = createReport();
            assertThat(started.await(5, TimeUnit.SECONDS)).isTrue();
            var pending = client.get(path);
            assertThat(pending.statusCode()).isEqualTo(202);
            assertThat(pending.body()).contains("CREATED");
        } finally {
            release.countDown();
        }

        var completed = awaitResult(path);
        assertThat(completed.statusCode()).isEqualTo(200);
        assertThat(completed.headers().firstValue("Content-Type")).hasValueSatisfying(
                value -> assertThat(value).startsWith("text/html").containsIgnoringCase("UTF-8"));
        assertThat(completed.body()).contains("Зарегистрированных пользователей: 1</p>",
                "<table>", "Проект &lt;script&gt;alert(1)&lt;/script&gt;",
                "Время подсчёта пользователей:", "Время получения списка проектов:",
                "Общее время формирования отчёта:").doesNotContain("<script>");
        var report = reports.findById(Long.parseLong(path.substring(path.lastIndexOf('/') + 1))).orElseThrow();
        assertThat(report.getStatus()).isEqualTo(ReportStatus.COMPLETED);
        assertThat(report.getContent()).isEqualTo(completed.body());
        assertThat(client.get(REPORTS + "/9223372036854775807").statusCode()).isEqualTo(404);
    }

    @Test
    void exposesErrorWhenWorkerFails() throws Exception {
        doThrow(new IllegalStateException("test worker failure")).when(projectQueries).findAll();

        var failed = awaitResult(createReport());

        assertThat(failed.statusCode()).isEqualTo(500);
        assertThat(failed.body()).contains("ERROR").doesNotContain("test worker failure");
    }

    private String createReport() throws Exception {
        var created = client.send("POST", REPORTS, null, null, true);
        assertThat(created.statusCode()).isEqualTo(202);
        var location = URI.create(created.headers().firstValue("Location").orElseThrow());
        assertThat(location.getPath()).matches(REPORTS + "/[0-9]+");
        assertThat(created.body()).contains(location.getPath().substring(REPORTS.length() + 1));
        return location.getPath();
    }

    private HttpResponse<String> awaitResult(String path) throws Exception {
        long deadline = System.nanoTime() + Duration.ofSeconds(5).toNanos();
        do {
            var response = client.get(path);
            if (response.statusCode() != 202) {
                return response;
            }
            Thread.sleep(25);
        } while (System.nanoTime() < deadline);
        throw new AssertionError("Report did not finish: " + path);
    }
}
