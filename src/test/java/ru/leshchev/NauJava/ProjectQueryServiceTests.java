package ru.leshchev.NauJava;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.leshchev.NauJava.dal.entity.Project;
import ru.leshchev.NauJava.dal.entity.ProjectStatus;
import ru.leshchev.NauJava.dal.repository.ProjectRepository;
import ru.leshchev.NauJava.service.ProjectQueryService;
import ru.leshchev.NauJava.web.dto.ProjectResponse;
import ru.leshchev.NauJava.web.error.ResourceNotFoundException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectQueryServiceTests {
    private static final Instant CREATED_AT = Instant.parse("2026-01-01T00:00:00Z");
    @Mock ProjectRepository repository;
    private ProjectQueryService service;

    @BeforeEach
    void setUp() {
        service = new ProjectQueryService(repository);
    }

    @Test
    void returnsAllProjectsInRepositoryOrder() {
        when(repository.findAllByOrderByIdAsc()).thenReturn(List.of(project(1L), project(2L)));

        assertEquals(List.of(response(1L), response(2L)), service.findAll());
    }

    @Test
    void returnsEmptyListWhenNameAndStatusDoNotMatch() {
        when(repository.findByNameAndStatusOrderByIdAsc("Missing", ProjectStatus.ACTIVE)).thenReturn(List.of());

        assertTrue(service.findByNameAndStatus("Missing", ProjectStatus.ACTIVE).isEmpty());
    }

    @Test
    void returnsProjectsForParticipantEmail() {
        when(repository.findByParticipantEmail("member@example.org")).thenReturn(List.of(project(1L)));

        assertEquals(List.of(response(1L)), service.findByParticipantEmail("member@example.org"));
    }

    @Test
    void returnsProjectById() {
        when(repository.findById(1L)).thenReturn(Optional.of(project(1L)));

        assertEquals(response(1L), service.findById(1L));
    }

    @Test
    void throwsWhenProjectDoesNotExist() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        var exception = assertThrows(ResourceNotFoundException.class, () -> service.findById(99L));
        assertEquals("Project 99 not found", exception.getMessage());
    }

    private Project project(long id) {
        return new Project().setId(id).setName("Project " + id).setDescription("Description")
                .setStatus(ProjectStatus.ACTIVE).setCreatedAt(CREATED_AT);
    }

    private ProjectResponse response(long id) {
        return new ProjectResponse(id, "Project " + id, "Description", ProjectStatus.ACTIVE, CREATED_AT);
    }
}
