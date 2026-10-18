package ru.leshchev.NauJava.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.leshchev.NauJava.dal.entity.Project;
import ru.leshchev.NauJava.dal.entity.ProjectTask;
import ru.leshchev.NauJava.dal.repository.ProjectRepository;
import ru.leshchev.NauJava.dal.repository.ProjectTaskRepository;

@Service
@RequiredArgsConstructor
public class ProjectLifecycleService {
    private final ProjectRepository projectRepository;
    private final ProjectTaskRepository taskRepository;

    @Transactional
    public Project createProjectWithInitialTask(Project project, ProjectTask task) {
        var savedProject = projectRepository.save(project);
        task.setProject(savedProject);
        savedProject.getTasks().add(task);
        taskRepository.save(task);
        return savedProject;
    }
}
