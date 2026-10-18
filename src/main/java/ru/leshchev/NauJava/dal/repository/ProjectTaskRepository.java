package ru.leshchev.NauJava.dal.repository;

import org.springframework.data.repository.CrudRepository;
import ru.leshchev.NauJava.dal.entity.ProjectTask;

public interface ProjectTaskRepository extends CrudRepository<ProjectTask, Long> {
}
