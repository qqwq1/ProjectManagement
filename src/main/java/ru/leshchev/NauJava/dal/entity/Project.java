package ru.leshchev.NauJava.dal.entity;

import lombok.Data;

import java.util.List;

@Data
public class Project {
    private Long id;
    private String name;
    private List<User> participants;
}
