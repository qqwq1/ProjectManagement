package ru.leshchev.NauJava.dal.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ru.leshchev.NauJava.dal.entity.ProjectTask;

@RepositoryRestResource(path = "projectTasks", collectionResourceRel = "projectTasks")
public interface ProjectTaskRepository extends CrudRepository<ProjectTask, Long> {
}
