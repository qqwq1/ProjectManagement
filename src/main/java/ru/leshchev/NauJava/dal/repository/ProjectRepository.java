package ru.leshchev.NauJava.dal.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import ru.leshchev.NauJava.dal.entity.Project;
import ru.leshchev.NauJava.dal.entity.ProjectStatus;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends CrudRepository<Project, Long> {
    @Override
    @EntityGraph(attributePaths = {"participants", "participants.user"})
    Optional<Project> findById(Long id);

    @EntityGraph(attributePaths = {"participants", "participants.user"})
    List<Project> findAllByOrderByIdAsc();

    List<Project> findByNameAndStatusOrderByIdAsc(String name, ProjectStatus status);

    @Query("""
            select distinct p from Project p
            join p.participants participant
            join participant.user u
            where u.email = :email
            order by p.id
            """)
    List<Project> findByParticipantEmail(@Param("email") String email);
}
