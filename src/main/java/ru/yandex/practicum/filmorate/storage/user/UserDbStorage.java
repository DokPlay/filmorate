package ru.yandex.practicum.filmorate.storage.user;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

/** JDBC implementation of {@link UserStorage}. */
@Repository
@Primary
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {

  private static final RowMapper<User> USER_MAPPER = (rs, rowNum) -> {
    final User user = new User();
    user.setId(rs.getLong("id"));
    user.setEmail(rs.getString("email"));
    user.setLogin(rs.getString("login"));
    user.setName(rs.getString("name"));
    final Date birthday = rs.getDate("birthday");
    if (birthday != null) {
      user.setBirthday(birthday.toLocalDate());
    }
    return user;
  };

  private final JdbcTemplate jdbcTemplate;

  @Override
  public List<User> findAll() {
    final String sql = "SELECT id, email, login, name, birthday FROM users ORDER BY id";
    final List<User> users = jdbcTemplate.query(sql, USER_MAPPER);
    enrichUsers(users);
    return users;
  }

  @Override
  public User getById(long id) {
    final String sql = "SELECT id, email, login, name, birthday FROM users WHERE id = ?";
    final List<User> users = jdbcTemplate.query(sql, USER_MAPPER, id);
    if (users.isEmpty()) {
      throw new NotFoundException("Пользователь с id=" + id + " не найден.");
    }
    enrichUsers(users);
    return users.get(0);
  }

  @Override
  public User create(User user) {
    final SimpleJdbcInsert insert = new SimpleJdbcInsert(jdbcTemplate)
        .withTableName("users")
        .usingGeneratedKeyColumns("id");
    final LocalDate birthday = user.getBirthday();
    final Map<String, Object> values = new LinkedHashMap<>(4);
    values.put("email", user.getEmail());
    values.put("login", user.getLogin());
    values.put("name", user.getName());
    values.put("birthday", birthday == null ? null : Date.valueOf(birthday));
    final Number id = insert.executeAndReturnKey(values);
    user.setId(id.longValue());
    updateFriends(user);
    return getById(user.getId());
  }

  @Override
  public User update(User user) {
    final String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
    final int updated = jdbcTemplate.update(sql,
        user.getEmail(),
        user.getLogin(),
        user.getName(),
        user.getBirthday() == null ? null : Date.valueOf(user.getBirthday()),
        user.getId());
    if (updated == 0) {
      throw new NotFoundException("Пользователь с id=" + user.getId() + " не найден.");
    }
    updateFriends(user);
    return getById(user.getId());
  }

  @Override
  public void delete(long id) {
    final int deleted = jdbcTemplate.update("DELETE FROM users WHERE id = ?", id);
    if (deleted == 0) {
      throw new NotFoundException("Пользователь с id=" + id + " не найден.");
    }
  }

  private void enrichUsers(List<User> users) {
    if (users.isEmpty()) {
      return;
    }
    final List<Long> userIds = users.stream()
        .map(User::getId)
        .distinct()
        .toList();
    final Map<Long, Set<Long>> friendsByUser = loadFriends(userIds);
    for (User user : users) {
      user.getFriends().clear();
      final Set<Long> friends = friendsByUser.get(user.getId());
      if (friends != null) {
        user.getFriends().addAll(friends);
      }
    }
  }

  private Map<Long, Set<Long>> loadFriends(List<Long> userIds) {
    if (userIds.isEmpty()) {
      return Collections.emptyMap();
    }
    final String placeholders = buildPlaceholders(userIds.size());
    final String sql = "SELECT user_id, friend_id FROM friendships WHERE user_id IN ("
        + placeholders + ") ORDER BY user_id, friend_id";
    return jdbcTemplate.query(sql, this::extractFriends, userIds.toArray(Object[]::new));
  }

  private String buildPlaceholders(int size) {
    return String.join(", ", Collections.nCopies(size, "?"));
  }

  private Map<Long, Set<Long>> extractFriends(ResultSet rs) throws SQLException {
    final Map<Long, Set<Long>> result = new LinkedHashMap<>();
    while (rs.next()) {
      final long userId = rs.getLong("user_id");
      result.computeIfAbsent(userId, key -> new LinkedHashSet<>())
          .add(rs.getLong("friend_id"));
    }
    return result;
  }

  private void updateFriends(User user) {
    if (user.getId() == null) {
      return;
    }
    jdbcTemplate.update("DELETE FROM friendships WHERE user_id = ?", user.getId());
    final Set<Long> friendIds = new LinkedHashSet<>(user.getFriends());
    final List<Object[]> batchArgs = new ArrayList<>(friendIds.size());
    final String sql =
        "INSERT INTO friendships (user_id, friend_id) VALUES (?, ?)";
    for (Long friendId : friendIds) {
      if (friendId == null || friendId.equals(user.getId())) {
        continue;
      }
      batchArgs.add(new Object[]{user.getId(), friendId});
    }
    if (!batchArgs.isEmpty()) {
      jdbcTemplate.batchUpdate(sql, batchArgs);
    }
  }
}
