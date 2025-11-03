package ru.yandex.practicum.filmorate.storage.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

/**
 * SPRINT 11:
 * - перенос хранения пользователей из сервиса в компонент-хранилище.
 * - генерация id внутри хранилища.
 */
@Component
public class InMemoryUserStorage implements UserStorage {

  private final Map<Long, User> users = new HashMap<>();
  private long idSeq = 0L;

  @Override
  public List<User> findAll() {
    return new ArrayList<>(users.values());
  }

  @Override
  public User getById(long id) {
    User user = users.get(id);
    if (user == null) {
      throw new NotFoundException("Пользователь с id=" + id + " не найден.");
    }
    return user;
  }

  @Override
  public User create(User user) {
    user.setId(++idSeq);              // SPRINT 11
    users.put(user.getId(), user);
    return user;
  }

  @Override
  public User update(User user) {
    if (user.getId() == null || !users.containsKey(user.getId())) {
      throw new NotFoundException("Пользователь с id=" + user.getId() + " не найден.");
    }
    users.put(user.getId(), user);
    return user;
  }

  @Override
  public void delete(long id) {
    if (!users.containsKey(id)) {
      throw new NotFoundException("Пользователь с id=" + id + " не найден.");
    }
    users.remove(id);
  }
}
