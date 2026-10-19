package ru.leshchev.NauJava.dal.repository;

import org.springframework.data.repository.CrudRepository;
import ru.leshchev.NauJava.dal.entity.ProjectParticipant;

public interface ProjectParticipantRepository extends CrudRepository<ProjectParticipant, Long> {
    boolean existsByProjectIdAndUserId(Long projectId, Long userId);
}
