package ru.leshchev.NauJava.dal.repository;

import org.springframework.data.repository.CrudRepository;
import ru.leshchev.NauJava.dal.entity.TaskComment;

public interface TaskCommentRepository extends CrudRepository<TaskComment, Long> {
}
