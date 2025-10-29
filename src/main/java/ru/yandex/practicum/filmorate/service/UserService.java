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
 *
 * FIX2:
 * - INFO для ключевых событий; полный payload не логируем (детали оставляем на уровень DEBUG при необходимости).
 * - nextId(): упрощено до `return ++idSeq;` (рекомендация ревьюера).
 * - Логи обновления дополнили login для наглядности.
 * - Добавлен alias-метод getAll() на findAll() для совместимости контроллеров.
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

  // FIX2: alias на случай, если контроллер вызывает getAll()
  public List<User> getAll() {
    return findAll();
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
    // CHANGE: ключевое событие в INFO (без полного дампа сущности)
    log.info("Создан пользователь id={} login='{}'", id, user.getLogin());
    return user;
  }

  public User update(User user) {
    if (user.getId() == null || !users.containsKey(user.getId())) {
      throw new NotFoundException("Пользователь с id=" + user.getId() + " не найден.");
    }
    normalize(user); // CHANGE
    users.put(user.getId(), user);
    // CHANGE: важное событие — INFO
    // FIX2: добавили login для контекста
    log.info("Обновлён пользователь id={} login='{}'", user.getId(), user.getLogin());
    return user;
  }

  // CHANGE: инкремент long-последовательности
  // FIX2: упрощённая форма — сразу вернуть инкрементированное значение
  private long nextId() {
    return ++idSeq;
  }

  // CHANGE: правило — если name пустой, подставляем login
  private void normalize(User user) {
    if (user.getName() == null || user.getName().isBlank()) {
      user.setName(user.getLogin());
    }
  }
}
