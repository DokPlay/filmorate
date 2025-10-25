package ru.yourname.filmorate.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yourname.filmorate.exception.NotFoundException;
import ru.yourname.filmorate.model.User;

@Slf4j
@Service
public class UserService {

  private final Map<Integer, User> users = new HashMap<>();
  private final AtomicInteger idSeq = new AtomicInteger(0);

  public User add(User user) {
    normalize(user);
    int id = idSeq.incrementAndGet();
    user.setId(id);
    users.put(id, user);
    log.info("Добавлен пользователь: {}", user);
    return user;
  }

  public User update(User user) {
    if (user.getId() == null || !users.containsKey(user.getId())) {
      throw new NotFoundException("Пользователь с id=" + user.getId() + " не найден.");
    }
    normalize(user);
    users.put(user.getId(), user);
    log.info("Обновлён пользователь: {}", user);
    return user;
  }

  public List<User> getAll() {
    return new ArrayList<>(users.values());
  }

  private void normalize(User user) {
    if (user.getName() == null || user.getName().isBlank()) {
      user.setName(user.getLogin());
    }
  }
}
