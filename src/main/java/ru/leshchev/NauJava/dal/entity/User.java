package ru.leshchev.NauJava.dal.entity;

import lombok.Data;

@Data
public class User {
    private String name;
    private String lastName;
    private Integer age;
    private String role;
}
