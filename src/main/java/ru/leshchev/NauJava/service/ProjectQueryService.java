package ru.leshchev.NauJava.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.leshchev.NauJava.dal.entity.ProjectStatus;
import ru.leshchev.NauJava.dal.repository.ProjectRepository;
import ru.leshchev.NauJava.web.dto.ProjectResponse;
import ru.leshchev.NauJava.web.error.ResourceNotFoundException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectQueryService {
    private final ProjectRepository repository;

    public List<ProjectResponse> findAll() {
        return repository.findAllByOrderByIdAsc().stream().map(ProjectResponse::from).toList();
    }

    public List<ProjectResponse> findByNameAndStatus(String name, ProjectStatus status) {
        return repository.findByNameAndStatusOrderByIdAsc(name, status).stream()
                .map(ProjectResponse::from).toList();
    }

    public List<ProjectResponse> findByParticipantEmail(String email) {
        return repository.findByParticipantEmail(email).stream().map(ProjectResponse::from).toList();
    }

    public ProjectResponse findById(Long id) {
        return repository.findById(id).map(ProjectResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Project " + id + " not found"));
    }
}
