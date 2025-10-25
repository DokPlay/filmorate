package ru.yandex.practicum.filmorate.exception;

/** Business validation exception. */
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
