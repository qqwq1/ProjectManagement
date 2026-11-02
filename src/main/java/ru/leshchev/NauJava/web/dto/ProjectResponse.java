package ru.leshchev.NauJava.web.dto;

import ru.leshchev.NauJava.dal.entity.Project;
import ru.leshchev.NauJava.dal.entity.ProjectStatus;
import java.time.Instant;

/** A flat response avoids serializing bidirectional JPA associations. */
public record ProjectResponse(Long id, String name, String description,
                              ProjectStatus status, Instant createdAt) {
    public static ProjectResponse from(Project project) {
        return new ProjectResponse(project.getId(), project.getName(), project.getDescription(),
                project.getStatus(), project.getCreatedAt());
    }
}
