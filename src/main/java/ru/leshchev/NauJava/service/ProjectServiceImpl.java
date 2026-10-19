package ru.leshchev.NauJava.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.leshchev.NauJava.dal.entity.Project;
import ru.leshchev.NauJava.dal.repository.ProjectRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectServiceImpl implements ProjectService {
    private final ProjectRepository projectRepository;

    @Override
    @Transactional
    public Project createProject(String name) {
        return projectRepository.save(new Project().setName(requireName(name)));
    }

    @Override
    public Project findById(Long id) {
        return projectRepository.findById(id).orElse(null);
    }

    @Override
    public List<Project> findAll() {
        return projectRepository.findAllByOrderByIdAsc();
    }

    @Override
    @Transactional
    public void updateProjectName(Long id, String newName) {
        requireProject(id).setName(requireName(newName));
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        projectRepository.delete(requireProject(id));
    }

    private Project requireProject(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Project with id " + id + " not found"));
    }

    private String requireName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Project name must not be blank");
        }
        return name.strip();
    }
}
