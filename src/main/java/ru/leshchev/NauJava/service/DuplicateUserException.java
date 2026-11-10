package ru.leshchev.NauJava.service;

public class DuplicateUserException extends RuntimeException {
    public DuplicateUserException() {
        super("Логин или email уже используется");
    }
}
