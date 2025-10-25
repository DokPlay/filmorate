package ru.yandex.practicum.filmorate.exception;

/** Resource not found exception. */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
