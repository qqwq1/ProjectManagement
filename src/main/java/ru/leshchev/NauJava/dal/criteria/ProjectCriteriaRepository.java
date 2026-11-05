package ru.leshchev.NauJava.dal.criteria;

import ru.leshchev.NauJava.dal.entity.Project;
import ru.leshchev.NauJava.dal.entity.ProjectStatus;

import java.util.List;

public interface ProjectCriteriaRepository {
    List<Project> findByNameAndStatusOrderByIdAsc(String name, ProjectStatus status);

    List<Project> findByParticipantEmail(String email);
}
