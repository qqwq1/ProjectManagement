package ru.leshchev.NauJava.console;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.leshchev.NauJava.dal.entity.Project;
import ru.leshchev.NauJava.service.ProjectService;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CommandProcessor {
    private final ProjectService projectService;

    public void processCommand(String input) {
        String[] command = input.trim().split("\\s+", 3);

        try {
            switch (command[0].toLowerCase()) {
                case "create" -> createProject(command);
                case "read" -> readProject(command);
                case "list" -> listProjects();
                case "update" -> updateProject(command);
                case "delete" -> deleteProject(command);
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

        Project project = projectService.createProject(getProjectName(command));
        System.out.println("Project created, " + formatProject(project));
    }

    private void readProject(String[] command) {
        requireArguments(command, 2, "Usage: read <id>");

        Project project = projectService.findById(Long.valueOf(command[1]));

        if (project == null) {
            System.out.println("Project not found.");
        } else {
            System.out.println(formatProject(project));
        }
    }

    private void listProjects() {
        List<Project> projects = projectService.findAll();

        if (projects.isEmpty()) {
            System.out.println("Projects list is empty.");
            return;
        }

        projects.forEach(project -> System.out.println(formatProject(project)));
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

    private void printHelp() {
        Arrays.asList(
                "Available commands:",
                "create <name>",
                "read <id>",
                "list",
                "update <id> <new_name>",
                "delete <id>",
                "exit"
        ).forEach(System.out::println);
    }

    private String getProjectName(String[] command) {
        if (command.length == 2) {
            return command[1];
        }

        return command[1] + " " + command[2];
    }

    private String formatProject(Project project) {
        return "id=" + project.getId()
                + ", name=" + project.getName()
                + ", participants=" + project.getParticipants();
    }

    private void requireArguments(String[] command, int expectedLength, String message) {
        if (command.length < expectedLength) {
            throw new IllegalArgumentException(message);
        }
    }
}
