package ru.leshchev.NauJava.service;

import ru.leshchev.NauJava.dal.entity.Project;

import java.util.List;

public interface ProjectService {
    Project createProject(String name);

    Project findById(Long id);

    List<Project> findAll();

    void updateProjectName(Long id, String newName);

    void deleteById(Long id);
}
