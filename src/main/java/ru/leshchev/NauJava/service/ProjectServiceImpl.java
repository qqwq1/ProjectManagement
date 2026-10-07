package ru.leshchev.NauJava.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.leshchev.NauJava.config.AppConfig;
import ru.leshchev.NauJava.dal.entity.Project;
import ru.leshchev.NauJava.dal.repository.CrudRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {
    private final CrudRepository<Project, Long> projectRepository;
    private final AppConfig appConfig;

    @PostConstruct
    public void printApplicationInfo() {
        System.out.printf("Application: %s, version: %s%n", appConfig.getAppName(), appConfig.getAppVersion());
    }

    @Override
    public Project createProject(String name) {
        Project project = new Project();
        project.setName(name);
        project.setParticipants(new ArrayList<>());
        projectRepository.create(project);
        return project;
    }

    @Override
    public Project findById(Long id) {
        return projectRepository.read(id);
    }

    @Override
    public List<Project> findAll() {
        return projectRepository.readAll();
    }

    @Override
    public void updateProjectName(Long id, String newName) {
        Project project = projectRepository.read(id);

        if (project == null) {
            throw new IllegalArgumentException("Project with id " + id + " not found");
        }

        project.setName(newName);
        projectRepository.update(project);
    }

    @Override
    public void deleteById(Long id) {
        projectRepository.delete(id);
    }
}
