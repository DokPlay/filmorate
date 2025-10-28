package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

/**
 * CHANGES:
 * - CHANGE: безопасное логирование (login/id вместо дампа сущности).
 * - CHANGE: согласование типов с Long id.
 */
@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @PostMapping
  public User create(@Valid @RequestBody User user) {
    // CHANGE: не логируем весь payload — только тех. поле
    log.debug("POST /users — создание: login='{}'", user.getLogin());
    return userService.create(user);
  }

  @PutMapping
  public User update(@Valid @RequestBody User user) {
    // CHANGE: аккуратный лог
    log.debug("PUT /users — обновление id={}", user.getId());
    return userService.update(user);
  }

  @GetMapping
  public List<User> findAll() {
    log.debug("GET /users — список");
    return userService.findAll();
  }

  @GetMapping("/{id}")
  public User getById(@PathVariable long id) { // CHANGE: long id
    log.debug("GET /users/{} — получить пользователя", id);
    return userService.getById(id);
  }
}
