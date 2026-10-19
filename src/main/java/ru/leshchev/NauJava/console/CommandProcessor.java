package ru.leshchev.NauJava.console;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.leshchev.NauJava.dal.entity.Project;
import ru.leshchev.NauJava.service.ProjectParticipantService;
import ru.leshchev.NauJava.service.ProjectService;

import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class CommandProcessor {
    private final ProjectService projectService;
    private final ProjectParticipantService participantService;

    public void processCommand(String input) {
        String[] command = input.strip().split("\\s+", 3);
        try {
            switch (command[0].toLowerCase(Locale.ROOT)) {
                case "create" -> createProject(command);
                case "read" -> readProject(command);
                case "list" -> listProjects();
                case "update" -> updateProject(command);
                case "delete" -> deleteProject(command);
                case "add-participant" -> addParticipant(input);
                case "help" -> printHelp();
                case "" -> {
                }
                default -> System.out.println("Unknown command. Type 'help' to see available commands.");
            }
        } catch (RuntimeException exception) {
            System.out.println("Command failed: " + exception.getMessage());
        }
    }

    private void createProject(String[] command) {
        requireArguments(command, 2, "Usage: create <name>");
        String name = command.length == 2 ? command[1] : command[1] + " " + command[2];
        System.out.println("Project created, " + formatProject(projectService.createProject(name)));
    }

    private void readProject(String[] command) {
        requireArguments(command, 2, "Usage: read <id>");
        Project project = projectService.findById(Long.valueOf(command[1]));
        System.out.println(project == null ? "Project not found." : formatProject(project));
    }

    private void listProjects() {
        var projects = projectService.findAll();
        if (projects.isEmpty()) {
            System.out.println("Projects list is empty.");
        } else {
            projects.forEach(project -> System.out.println(formatProject(project)));
        }
    }

    private void updateProject(String[] command) {
        requireArguments(command, 3, "Usage: update <id> <new_name>");
        projectService.updateProjectName(Long.valueOf(command[1]), command[2]);
        System.out.println("Project updated.");
    }

    private void deleteProject(String[] command) {
        requireArguments(command, 2, "Usage: delete <id>");
        projectService.deleteById(Long.valueOf(command[1]));
        System.out.println("Project deleted.");
    }

    private void addParticipant(String input) {
        String[] command = input.strip().split("\\s+", 5);
        requireArguments(command, 5, "Usage: add-participant <project_id> <email> <name> <last_name>");
        var participant = participantService.add(Long.valueOf(command[1]), command[2], command[3], command[4]);
        System.out.println("Participant added, id=" + participant.getId() + ", email=" + participant.getUser().getEmail());
    }

    private void printHelp() {
        List.of(
                "Available commands:",
                "create <name>",
                "read <id>",
                "list",
                "update <id> <new_name>",
                "delete <id>",
                "add-participant <project_id> <email> <name> <last_name>",
                "help",
                "exit"
        ).forEach(System.out::println);
    }

    private String formatProject(Project project) {
        var participants = project.getParticipants().stream()
                .map(participant -> participant.getUser().getName() + " "
                        + participant.getUser().getLastName() + " (" + participant.getUser().getEmail() + ")")
                .toList();
        return "id=" + project.getId() + ", name=" + project.getName() + ", participants=" + participants;
    }

    private void requireArguments(String[] command, int expectedLength, String message) {
        if (command.length < expectedLength) {
            throw new IllegalArgumentException(message);
        }
    }
}
