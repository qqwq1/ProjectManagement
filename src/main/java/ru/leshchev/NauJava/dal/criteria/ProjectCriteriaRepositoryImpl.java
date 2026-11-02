package ru.leshchev.NauJava.dal.criteria;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.leshchev.NauJava.dal.entity.Project;
import ru.leshchev.NauJava.dal.entity.ProjectParticipant;
import ru.leshchev.NauJava.dal.entity.ProjectStatus;
import ru.leshchev.NauJava.dal.entity.User;

import java.util.List;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectCriteriaRepositoryImpl implements ProjectCriteriaRepository {
    private final EntityManager entityManager;

    @Override
    public List<Project> findByNameAndStatusOrderByIdAsc(String name, ProjectStatus status) {
        var builder = entityManager.getCriteriaBuilder();
        var query = builder.createQuery(Project.class);
        Root<Project> project = query.from(Project.class);
        query.select(project).where(builder.and(
                name == null ? builder.isNull(project.get("name")) : builder.equal(project.get("name"), name),
                status == null ? builder.isNull(project.get("status")) : builder.equal(project.get("status"), status)
        )).orderBy(builder.asc(project.get("id")));
        return entityManager.createQuery(query).getResultList();
    }

    @Override
    public List<Project> findByParticipantEmail(String email) {
        var builder = entityManager.getCriteriaBuilder();
        var query = builder.createQuery(Project.class);
        Root<Project> project = query.from(Project.class);
        Join<Project, ProjectParticipant> participant = project.join("participants");
        Join<ProjectParticipant, User> user = participant.join("user");
        query.select(project).distinct(true)
                .where(builder.equal(user.get("email"), email))
                .orderBy(builder.asc(project.get("id")));
        return entityManager.createQuery(query).getResultList();
    }
}
