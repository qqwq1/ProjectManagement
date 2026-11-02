package ru.leshchev.NauJava.dal.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ru.leshchev.NauJava.dal.entity.TaskComment;

@RepositoryRestResource(path = "taskComments", collectionResourceRel = "taskComments")
public interface TaskCommentRepository extends CrudRepository<TaskComment, Long> {
}
