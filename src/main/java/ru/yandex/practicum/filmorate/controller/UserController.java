package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
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
 *
 * FIX2:
 * - INFO для ключевых событий (create/update/getById) с краткими параметрами.
 * - DEBUG для детальных сведений (полный payload), включаем при отладке.
 *
 * SPRINT 11:
 * - добавлены REST-эндпоинты дружбы:
 *   PUT /users/{id}/friends/{friendId} — добавить в друзья
 *   DELETE /users/{id}/friends/{friendId} — удалить из друзей
 *   GET /users/{id}/friends — список друзей
 *   GET /users/{id}/friends/common/{otherId} — общие друзья
 */
@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @PostMapping
  public User create(@Valid @RequestBody final User user) {
    // CHANGE: не логируем весь payload — только тех. поле
    // FIX2: ключевое событие — на уровне INFO
    log.info("POST /users — create login='{}'", user.getLogin());
    // FIX2: полный payload — в DEBUG
    log.debug("POST /users payload: {}", user);
    return userService.create(user);
  }

  @PutMapping
  public User update(@Valid @RequestBody final User user) {
    // CHANGE: аккуратный лог
    // FIX2: важное событие — INFO + краткий контекст
    log.info("PUT /users — update id={} login='{}'", user.getId(), user.getLogin());
    // FIX2: полный payload — в DEBUG
    log.debug("PUT /users payload: {}", user);
    return userService.update(user);
  }

  @GetMapping
  public List<User> findAll() {
    // FIX2: список — обычная операция, оставляем в DEBUG
    log.debug("GET /users — list");
    return userService.findAll();
  }

  @GetMapping("/{id}")
  public User getById(@PathVariable final long id) { // CHANGE: long id
    // FIX2: входной параметр важен — логируем на INFO
    log.info("GET /users/{} — fetch", id);
    return userService.getById(id);
  }

  // ----------- SPRINT 11: дружба -----------

  @PutMapping("/{id}/friends/{friendId}")
  public void addFriend(@PathVariable final long id, @PathVariable final long friendId) {
    log.info("PUT /users/{}/friends/{} — add", id, friendId);
    userService.addFriend(id, friendId);
  }

  @DeleteMapping("/{id}/friends/{friendId}")
  public void removeFriend(@PathVariable final long id, @PathVariable final long friendId) {
    log.info("DELETE /users/{}/friends/{} — remove", id, friendId);
    userService.removeFriend(id, friendId);
  }

  @GetMapping("/{id}/friends")
  public List<User> getFriends(@PathVariable final long id) {
    log.debug("GET /users/{}/friends — list", id);
    return userService.getFriends(id);
  }

  @GetMapping("/{id}/friends/common/{otherId}")
  public List<User> getCommonFriends(@PathVariable final long id, @PathVariable final long otherId) {
    log.debug("GET /users/{}/friends/common/{} — list", id, otherId);
    return userService.getCommonFriends(id, otherId);
  }
}
