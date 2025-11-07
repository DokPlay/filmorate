package ru.yandex.practicum.filmorate.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import lombok.RequiredArgsConstructor;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmDbStorage.class, UserDbStorage.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmDbStorageTest {

  private final FilmDbStorage filmStorage;
  private final UserDbStorage userStorage;

  @Test
  void createAndFindById() {
    final Film film = filmStorage.create(sampleFilm("Inception"));

    final Film found = filmStorage.getById(film.getId());

    assertThat(found.getName()).isEqualTo("Inception");
    assertThat(found.getMpa().getName()).isEqualTo("G");
    assertThat(found.getGenres()).extracting(Genre::getId).containsExactly(1, 2);
  }

  @Test
  void updateShouldPersistGenresAndLikes() {
    final Film film = filmStorage.create(sampleFilm("Interstellar"));
    final User user = userStorage.create(sampleUser("nolan@example.com", "nolan"));

    film.setDescription("Updated description");
    film.setGenres(new LinkedHashSet<>(List.of(new Genre(2, null), new Genre(3, null))));
    film.getLikes().add(user.getId());

    filmStorage.update(film);

    final Film updated = filmStorage.getById(film.getId());
    assertThat(updated.getDescription()).isEqualTo("Updated description");
    assertThat(updated.getGenres()).extracting(Genre::getId).containsExactly(2, 3);
    assertThat(updated.getLikes()).containsExactly(user.getId());
  }

  @Test
  void findAllReturnsAllFilms() {
    final Film first = filmStorage.create(sampleFilm("Film A"));
    final Film second = filmStorage.create(sampleFilm("Film B"));

    final List<Film> all = filmStorage.findAll();

    assertThat(all).extracting(Film::getId).containsExactlyInAnyOrder(first.getId(), second.getId());
  }

  @Test
  void findMostPopularReturnsFilmsOrderedByLikes() {
    final Film first = filmStorage.create(sampleFilm("Popular"));
    final Film second = filmStorage.create(sampleFilm("Less Popular"));

    final User u1 = userStorage.create(sampleUser("user1@example.com", "user1"));
    final User u2 = userStorage.create(sampleUser("user2@example.com", "user2"));
    final User u3 = userStorage.create(sampleUser("user3@example.com", "user3"));

    first.getLikes().add(u1.getId());
    first.getLikes().add(u2.getId());
    second.getLikes().add(u3.getId());
    filmStorage.update(first);
    filmStorage.update(second);

    final List<Film> popular = filmStorage.findMostPopular(1);

    assertThat(popular).hasSize(1);
    assertThat(popular.get(0).getId()).isEqualTo(first.getId());
  }

  @Test
  void deleteRemovesFilm() {
    final Film film = filmStorage.create(sampleFilm("To Delete"));

    filmStorage.delete(film.getId());

    assertThatThrownBy(() -> filmStorage.getById(film.getId()))
        .isInstanceOf(NotFoundException.class);
  }

  private Film sampleFilm(String name) {
    final Film film = new Film();
    film.setName(name);
    film.setDescription("Test description");
    film.setReleaseDate(LocalDate.of(2000, 1, 1));
    film.setDuration(120);
    film.setMpa(new Mpa(1, null));
    film.setGenres(new LinkedHashSet<>(List.of(new Genre(1, null), new Genre(2, null))));
    return film;
  }

  private User sampleUser(String email, String login) {
    final User user = new User();
    user.setEmail(email);
    user.setLogin(login);
    user.setName("User");
    user.setBirthday(LocalDate.of(1990, 1, 1));
    return user;
  }
}
