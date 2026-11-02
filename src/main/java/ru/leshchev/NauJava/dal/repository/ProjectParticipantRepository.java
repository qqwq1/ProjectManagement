package ru.leshchev.NauJava.dal.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ru.leshchev.NauJava.dal.entity.ProjectParticipant;

@RepositoryRestResource(path = "projectParticipants", collectionResourceRel = "projectParticipants")
public interface ProjectParticipantRepository extends CrudRepository<ProjectParticipant, Long> {
    boolean existsByProjectIdAndUserId(@Param("projectId") Long projectId, @Param("userId") Long userId);
}
