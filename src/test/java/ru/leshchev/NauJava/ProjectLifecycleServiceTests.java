package ru.leshchev.NauJava;

import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.leshchev.NauJava.dal.entity.Project;
import ru.leshchev.NauJava.dal.entity.ProjectTask;
import ru.leshchev.NauJava.dal.repository.ProjectRepository;
import ru.leshchev.NauJava.dal.repository.ProjectTaskRepository;
import ru.leshchev.NauJava.service.ProjectLifecycleService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ProjectLifecycleServiceTests {
    private final ProjectLifecycleService lifecycleService;
    private final ProjectRepository projectRepository;
    private final ProjectTaskRepository taskRepository;

    @AfterEach
    void cleanDatabase() {
        taskRepository.deleteAll();
        projectRepository.deleteAll();
    }

    @Test
    void createsProjectAndInitialTask() {
        var task = new ProjectTask().setTitle("Первая задача");
        var project = lifecycleService.createProjectWithInitialTask(
                new Project().setName("Запуск проекта"), task);

        assertThat(projectRepository.existsById(project.getId())).isTrue();
        var savedTask = taskRepository.findById(task.getId()).orElseThrow();
        assertThat(savedTask.getProject().getId()).isEqualTo(project.getId());
        assertThat(savedTask.getTitle()).isEqualTo("Первая задача");
    }

    @Test
    void rollsBackProjectWhenTaskIsInvalid() {
        assertThatThrownBy(() -> lifecycleService.createProjectWithInitialTask(
                new Project().setName("Откатить проект"),
                new ProjectTask().setTitle(" ")))
                .isInstanceOf(ConstraintViolationException.class);

        assertThat(projectRepository.count()).isZero();
        assertThat(taskRepository.count()).isZero();
    }
}
