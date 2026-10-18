package ru.leshchev.NauJava;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.leshchev.NauJava.dal.criteria.ProjectCriteriaRepository;
import ru.leshchev.NauJava.dal.entity.Project;
import ru.leshchev.NauJava.dal.entity.ProjectParticipant;
import ru.leshchev.NauJava.dal.entity.ProjectStatus;
import ru.leshchev.NauJava.dal.entity.User;
import ru.leshchev.NauJava.dal.repository.ProjectParticipantRepository;
import ru.leshchev.NauJava.dal.repository.ProjectRepository;
import ru.leshchev.NauJava.dal.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class RepositoryQueryTests {
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectParticipantRepository participantRepository;
    private final ProjectCriteriaRepository criteriaRepository;
    private Project project;

    @BeforeEach
    void setUp() {
        project = projectRepository.save(new Project().setName("Платформа").setStatus(ProjectStatus.ACTIVE));
        projectRepository.save(new Project().setName("Платформа").setStatus(ProjectStatus.COMPLETED));
        projectRepository.save(new Project().setName("Другой").setStatus(ProjectStatus.ACTIVE));
        var user = userRepository.save(new User()
                .setName("Анна")
                .setLastName("Иванова")
                .setEmail("anna@example.org"));
        participantRepository.save(new ProjectParticipant().setProject(project).setUser(user));
    }

    @Test
    void findsByNameAndStatus() {
        assertThat(projectRepository.findByNameAndStatusOrderByIdAsc("Платформа", ProjectStatus.ACTIVE))
                .extracting(Project::getId).containsExactly(project.getId());
        assertThat(projectRepository.findByNameAndStatusOrderByIdAsc("Нет такого", ProjectStatus.ACTIVE))
                .isEmpty();
    }

    @Test
    void findsByParticipantEmail() {
        assertThat(projectRepository.findByParticipantEmail("anna@example.org"))
                .extracting(Project::getId).containsExactly(project.getId());
        assertThat(projectRepository.findByParticipantEmail("nobody@example.org")).isEmpty();
    }

    @Test
    void findsByNameAndStatusUsingCriteria() {
        assertThat(criteriaRepository.findByNameAndStatusOrderByIdAsc("Платформа", ProjectStatus.ACTIVE))
                .extracting(Project::getId).containsExactly(project.getId());
        assertThat(criteriaRepository.findByNameAndStatusOrderByIdAsc("Нет такого", ProjectStatus.ACTIVE))
                .isEmpty();
    }

    @Test
    void findsByParticipantEmailUsingCriteria() {
        assertThat(criteriaRepository.findByParticipantEmail("anna@example.org"))
                .extracting(Project::getId).containsExactly(project.getId());
        assertThat(criteriaRepository.findByParticipantEmail("nobody@example.org")).isEmpty();
    }
}
