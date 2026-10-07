package ru.leshchev.NauJava.dal.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import ru.leshchev.NauJava.dal.entity.Project;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

@Repository
@RequiredArgsConstructor
public class CrudRepositoryImpl implements CrudRepository<Project, Long> {
    @Qualifier("projectContainer")
    private final List<Project> projectsStorage;

    @Override
    public void create(Project entity) {
        Objects.requireNonNull(entity, "Project must not be null");

        if (entity.getId() == null) {
            entity.setId(generateId());
        } else if (existsById(entity.getId())) {
            throw new IllegalArgumentException("Project with id " + entity.getId() + " already exists");
        }

        projectsStorage.add(entity);
    }

    @Override
    public Project read(Long id) {
        return projectsStorage.stream()
                .filter(project -> Objects.equals(project.getId(), id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<Project> readAll() {
        return List.copyOf(projectsStorage);
    }

    @Override
    public void update(Project entity) {
        Objects.requireNonNull(entity, "Project must not be null");
        Objects.requireNonNull(entity.getId(), "Project id must not be null");

        for (int i = 0; i < projectsStorage.size(); i++) {
            if (Objects.equals(projectsStorage.get(i).getId(), entity.getId())) {
                projectsStorage.set(i, entity);
                return;
            }
        }

        throw new NoSuchElementException("Project with id " + entity.getId() + " not found");
    }

    @Override
    public void delete(Long id) {
        boolean removed = projectsStorage.removeIf(project -> Objects.equals(project.getId(), id));

        if (!removed) {
            throw new NoSuchElementException("Project with id " + id + " not found");
        }
    }

    private Long generateId() {
        return projectsStorage.stream()
                .map(Project::getId)
                .filter(Objects::nonNull)
                .max(Long::compareTo)
                .orElse(0L) + 1;
    }

    private boolean existsById(Long id) {
        return projectsStorage.stream()
                .anyMatch(project -> Objects.equals(project.getId(), id));
    }
}
