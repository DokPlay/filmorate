package ru.yandex.practicum.filmorate.exception;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * CHANGES:
 * - CHANGE: Объединён обработчик 400 для Bean-валидации и бизнес-валидации в ОДИН метод
 *   через @ExceptionHandler({ MethodArgumentNotValidException.class, ValidationException.class }).
 * - CHANGE: Единообразный ответ для 400 (message + по необходимости errors).
 * - CHANGE: Отдельный 404 для NotFoundException.
 * - CHANGE: Фолбэк 500 для прочих ошибок.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  // CHANGE: объединённый BAD_REQUEST-хендлер
  @ExceptionHandler({MethodArgumentNotValidException.class, ValidationException.class})
  public ResponseEntity<Map<String, Object>> handleBadRequest(Exception ex) {
    Map<String, Object> body = new HashMap<>();
    if (ex instanceof MethodArgumentNotValidException manve) {
      // CHANGE: для bean-валидации возвращаем map поле->сообщение
      Map<String, String> errors = new HashMap<>();
      for (FieldError fe : manve.getBindingResult().getFieldErrors()) {
        errors.put(fe.getField(), fe.getDefaultMessage());
      }
      log.warn("Ошибка валидации запроса: {}", errors);
      body.put("message", "Validation failed");
      body.put("errors", errors);
    } else {
      // CHANGE: бизнес-валидация отдаёт единый message
      String msg = ex.getMessage() == null ? "Validation error" : ex.getMessage();
      log.warn("Ошибка бизнес-валидации: {}", msg);
      body.put("message", msg);
    }
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
  }

  // CHANGE: отдельный обработчик 404
  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<Map<String, Object>> handleNotFound(NotFoundException ex) {
    String msg = ex.getMessage() == null ? "Resource not found" : ex.getMessage();
    log.warn("Ресурс не найден: {}", msg);
    Map<String, Object> body = new HashMap<>();
    body.put("message", msg);
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
  }

  // CHANGE: общий фолбэк 500 с логом стектрейса
  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleOther(Exception ex) {
    log.error("Необработанное исключение", ex);
    Map<String, Object> body = new HashMap<>();
    body.put("message", "Internal server error");
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
  }
}
