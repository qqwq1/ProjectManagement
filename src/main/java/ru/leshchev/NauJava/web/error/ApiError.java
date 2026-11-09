package ru.leshchev.NauJava.web.error;

public record ApiError(int status, String error, String message, String path) {
}
