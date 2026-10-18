package ru.leshchev.NauJava.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.leshchev.NauJava.dal.entity.ProjectParticipant;
import ru.leshchev.NauJava.dal.entity.User;
import ru.leshchev.NauJava.dal.repository.ProjectParticipantRepository;
import ru.leshchev.NauJava.dal.repository.ProjectRepository;
import ru.leshchev.NauJava.dal.repository.UserRepository;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ProjectParticipantService {
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectParticipantRepository participantRepository;

    @Transactional
    public ProjectParticipant add(Long projectId, String email, String name, String lastName) {
        var project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project with id " + projectId + " not found"));
        String normalizedEmail = email.strip().toLowerCase(Locale.ROOT);
        var user = userRepository.findByEmail(normalizedEmail)
                .orElseGet(() -> userRepository.save(new User()
                        .setName(name.strip())
                        .setLastName(lastName.strip())
                        .setEmail(normalizedEmail)));
        if (participantRepository.existsByProjectIdAndUserId(projectId, user.getId())) {
            throw new IllegalArgumentException("User already participates in this project");
        }
        var participant = new ProjectParticipant().setProject(project).setUser(user);
        project.getParticipants().add(participant);
        return participantRepository.save(participant);
    }
}
