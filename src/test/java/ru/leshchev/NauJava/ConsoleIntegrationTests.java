package ru.leshchev.NauJava;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import ru.leshchev.NauJava.console.CommandProcessor;
import ru.leshchev.NauJava.dal.entity.ProjectStatus;
import ru.leshchev.NauJava.dal.repository.ProjectParticipantRepository;
import ru.leshchev.NauJava.dal.repository.ProjectRepository;
import ru.leshchev.NauJava.dal.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ExtendWith(OutputCaptureExtension.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ConsoleIntegrationTests {
    private final CommandProcessor commandProcessor;
    private final ProjectRepository projectRepository;
    private final ProjectParticipantRepository participantRepository;
    private final UserRepository userRepository;

    @AfterEach
    void cleanDatabase() {
        projectRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void projectCrudAndParticipantAreAvailableThroughConsole(CapturedOutput output) {
        commandProcessor.processCommand("create Консольный проект");
        var project = projectRepository.findByNameAndStatusOrderByIdAsc("Консольный проект", ProjectStatus.PLANNED).getFirst();
        Long id = project.getId();

        commandProcessor.processCommand("read " + id);
        commandProcessor.processCommand("update " + id + " Новый проект");
        assertThat(projectRepository.findById(id).orElseThrow().getName()).isEqualTo("Новый проект");

        String addParticipant = "add-participant " + id + " ANNA@EXAMPLE.ORG Анна Иванова";
        commandProcessor.processCommand(addParticipant);
        commandProcessor.processCommand(addParticipant);
        assertThat(participantRepository.count()).isEqualTo(1);
        commandProcessor.processCommand("list");
        assertThat(output).contains("Project created", "Project updated.",
                "name=Консольный проект", "name=Новый проект",
                "Анна Иванова (anna@example.org)", "User already participates in this project");

        commandProcessor.processCommand("delete " + id);
        assertThat(projectRepository.existsById(id)).isFalse();
        assertThat(participantRepository.count()).isZero();
        assertThat(userRepository.count()).isEqualTo(1);
        commandProcessor.processCommand("read " + id);
        commandProcessor.processCommand("list");
        commandProcessor.processCommand("create");
        commandProcessor.processCommand("read invalid");
        assertThat(output).contains("Project deleted.", "Project not found.",
                "Projects list is empty.", "Usage: create <name>", "Command failed:");
    }
}
