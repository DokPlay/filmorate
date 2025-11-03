package ru.yandex.practicum.filmorate.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

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
 *
 * SPRINT 11:
 * - сервис зависит от интерфейса UserStorage (DI через конструктор, @RequiredArgsConstructor).
 * - хранение данных вынесено в InMemoryUserStorage; генерация id выполняется в storage.
 * - добавлены операции дружбы: addFriend/removeFriend/getFriends/getCommonFriends.
 * - методы работают на уровне INFO для ключевых событий.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

  // SPRINT 11: внедрение хранилища пользователей через интерфейс
  private final UserStorage userStorage;

  public List<User> findAll() {
    // CHANGE: типобезопасный List (теперь приходит из хранилища)
    return userStorage.findAll();
  }

  // FIX2: alias на случай, если контроллер вызывает getAll()
  public List<User> getAll() {
    return findAll();
  }

  public User getById(final long id) {
    return userStorage.getById(id);
  }

  public User create(final User user) {
    normalize(user); // CHANGE: автоподстановка name
    final User saved = userStorage.create(user); // SPRINT 11
    // CHANGE: ключевое событие в INFO (без полного дампа сущности)
    log.info("Создан пользователь id={} login='{}'", saved.getId(), saved.getLogin());
    return saved;
  }

  public User update(final User user) {
    if (user.getId() == null) {
      // SPRINT 11: явная валидация входных данных
      throw new ValidationException("id обязателен для обновления пользователя.");
    }
    normalize(user); // CHANGE
    final User saved = userStorage.update(user); // SPRINT 11
    // CHANGE + FIX2: важное событие — INFO, добавили login для контекста
    log.info("Обновлён пользователь id={} login='{}'", saved.getId(), saved.getLogin());
    return saved;
  }

  // ----------- SPRINT 11: дружба -----------

  public void addFriend(final long id, final long friendId) {
    if (id == friendId) {
      throw new ValidationException("Нельзя добавить в друзья самого себя.");
    }
    final User u = userStorage.getById(id);
    final User f = userStorage.getById(friendId);

    final boolean a = u.getFriends().add(friendId);
    final boolean b = f.getFriends().add(id);
    if (a || b) {
      userStorage.update(u);
      userStorage.update(f);
      log.info("Пользователи стали друзьями: {} <-> {}", id, friendId);
    } else {
      log.debug("Повторное добавление в друзья проигнорировано: {} <-> {}", id, friendId);
    }
  }

  public void removeFriend(final long id, final long friendId) {
    final User u = userStorage.getById(id);
    final User f = userStorage.getById(friendId);

    final boolean a = u.getFriends().remove(friendId);
    final boolean b = f.getFriends().remove(id);
    if (a || b) {
      userStorage.update(u);
      userStorage.update(f);
      log.info("Дружба удалена: {} X {}", id, friendId);
    } else {
      log.debug("Удаление дружбы: связи не было {} X {}", id, friendId);
    }
  }

  public List<User> getFriends(final long id) {
    final User u = userStorage.getById(id);
    return idsToUsers(u.getFriends());
  }

  public List<User> getCommonFriends(final long id, final long otherId) {
    final User a = userStorage.getById(id);
    final User b = userStorage.getById(otherId);
    final Set<Long> common = a.getFriends().stream()
        .filter(b.getFriends()::contains)
        .collect(Collectors.toSet());
    return idsToUsers(common);
  }

  // ----------- утилиты -----------

  private List<User> idsToUsers(final Set<Long> ids) {
    final List<User> list = new ArrayList<>();
    for (Long i : ids) {
      list.add(userStorage.getById(i));
    }
    return list;
  }

  // CHANGE: правило — если name пустой, подставляем login
  private void normalize(final User user) {
    if (user.getName() == null || user.getName().isBlank()) {
      user.setName(user.getLogin());
    }
  }
}
