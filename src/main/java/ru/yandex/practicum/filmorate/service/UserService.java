package ru.yandex.practicum.filmorate.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

/**
 * CHANGES:
 * - CHANGE: Map -> Map<Long, User>, idSeq: AtomicInteger -> long.
 * - CHANGE: normalize(): если name пустой — подставляем login (требование курса).
 * - CHANGE: безопасное логирование (id/login вместо дампа сущности).
 */
@Slf4j
@Service
public class UserService {

  // CHANGE: ключи теперь Long
  private final Map<Long, User> users = new HashMap<>();

  // CHANGE: счётчик без Atomic*
  private long idSeq = 0L;

  public List<User> findAll() {
    // CHANGE: типобезопасный List
    return new ArrayList<>(users.values());
  }

  public User getById(long id) {
    User user = users.get(id);
    if (user == null) {
      throw new NotFoundException("Пользователь с id=" + id + " не найден.");
    }
    return user;
  }

  public User create(User user) {
    normalize(user); // CHANGE: автоподстановка name
    long id = nextId(); // CHANGE: long id
    user.setId(id);
    users.put(id, user);
    log.info("Создан пользователь id={} login='{}'", id, user.getLogin()); // CHANGE
    return user;
  }

  public User update(User user) {
    if (user.getId() == null || !users.containsKey(user.getId())) {
      throw new NotFoundException("Пользователь с id=" + user.getId() + " не найден.");
    }
    normalize(user); // CHANGE
    users.put(user.getId(), user);
    log.info("Обновлён пользователь id={}", user.getId()); // CHANGE
    return user;
  }

  // CHANGE: инкремент long-последовательности
  private long nextId() {
    idSeq += 1L;
    return idSeq;
  }

  // CHANGE: правило — если name пустой, подставляем login
  private void normalize(User user) {
    if (user.getName() == null || user.getName().isBlank()) {
      user.setName(user.getLogin());
    }
  }
}
