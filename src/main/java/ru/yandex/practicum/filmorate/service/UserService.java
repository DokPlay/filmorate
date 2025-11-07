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
 *
 * SPRINT 12 (fix):
 * - removeFriend стал симметричным: при наличии взаимной дружбы удаляем id у friendId и сохраняем обе стороны.
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

    final boolean added = u.getFriends().add(friendId);
    if (added) {
      userStorage.update(u);
      if (f.getFriends().contains(id)) {
        log.info("Дружба подтверждена: {} <-> {}", id, friendId);
      } else {
        log.info("Пользователь {} отправил заявку в друзья пользователю {}", id, friendId);
      }
    } else {
      log.debug("Повторное добавление в друзья проигнорировано: {} <-> {}", id, friendId);
    }
  }

  public void removeFriend(final long id, final long friendId) {
    // fix sprint 12: симметрично разрываем дружбу и сохраняем обе стороны при необходимости
    final User u = userStorage.getById(id);
    final User f = userStorage.getById(friendId);

    final boolean removedFromU = u.getFriends().remove(friendId);
    boolean removedFromF = false;

    // Если дружба была взаимной — удаляем id у друга
    if (f.getFriends().contains(id)) {
      removedFromF = f.getFriends().remove(id);
    }

    if (removedFromU) {
      userStorage.update(u);
    }
    if (removedFromF) {
      userStorage.update(f);
    }

    if (removedFromU && removedFromF) {
      log.info("Дружба разорвана: {} <-> {}", id, friendId);
    } else if (removedFromU) {
      log.info("Пользователь {} удалил из друзей пользователя {}", id, friendId);
    } else if (removedFromF) {
      // Неконсистентный случай: у друга была запись, у текущего — нет; подчистили.
      log.info("Подчистка неконсистентной записи дружбы: удалили {} из друзей {}", id, friendId);
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
