package ru.yandex.practicum.filmorate.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import lombok.RequiredArgsConstructor;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

@JdbcTest
@AutoConfigureTestDatabase
@Import(UserDbStorage.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserDbStorageTest {

  private final UserDbStorage userStorage;

  @Test
  void createAndFindById() {
    final User created = userStorage.create(sampleUser("alice@example.com", "alice"));

    final User found = userStorage.getById(created.getId());

    assertThat(found.getId()).isEqualTo(created.getId());
    assertThat(found.getEmail()).isEqualTo("alice@example.com");
    assertThat(found.getLogin()).isEqualTo("alice");
  }

  @Test
  void updateShouldPersistFriends() {
    final User user = userStorage.create(sampleUser("bob@example.com", "bob"));
    final User friend = userStorage.create(sampleUser("carol@example.com", "carol"));

    user.getFriends().add(friend.getId());
    user.setName("Robert");
    userStorage.update(user);

    final User reloaded = userStorage.getById(user.getId());
    assertThat(reloaded.getFriends()).containsExactly(friend.getId());
    assertThat(reloaded.getName()).isEqualTo("Robert");

    final User friendReloaded = userStorage.getById(friend.getId());
    assertThat(friendReloaded.getFriends()).doesNotContain(user.getId());
  }

  @Test
  void findAllReturnsAllUsers() {
    final User first = userStorage.create(sampleUser("dave@example.com", "dave"));
    final User second = userStorage.create(sampleUser("erin@example.com", "erin"));

    final List<User> all = userStorage.findAll();

    assertThat(all).extracting(User::getId).containsExactlyInAnyOrder(first.getId(), second.getId());
  }

  @Test
  void deleteRemovesUser() {
    final User user = userStorage.create(sampleUser("frank@example.com", "frank"));

    userStorage.delete(user.getId());

    assertThatThrownBy(() -> userStorage.getById(user.getId()))
        .isInstanceOf(NotFoundException.class);
  }

  private User sampleUser(String email, String login) {
    final User user = new User();
    user.setEmail(email);
    user.setLogin(login);
    user.setName("Test User");
    user.setBirthday(LocalDate.of(1990, 1, 1));
    return user;
  }
}
