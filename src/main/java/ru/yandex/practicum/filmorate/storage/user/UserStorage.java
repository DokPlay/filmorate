package ru.yandex.practicum.filmorate.storage.user;

import java.util.List;
import ru.yandex.practicum.filmorate.model.User;

/**
 * CHANGES:
 * - SPRINT 11: добавлен интерфейс слоя хранения пользователей.
 *
 * SPRINT 11 (checkstyle):
 * - добавлены пустые строки между методами (EmptyLineSeparator).
 */
public interface UserStorage {

  List<User> findAll();

  User getById(long id);

  User create(User user);

  User update(User user);

  void delete(long id);
}
