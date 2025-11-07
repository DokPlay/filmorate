package ru.yandex.practicum.filmorate.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import lombok.RequiredArgsConstructor;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;

@JdbcTest
@AutoConfigureTestDatabase
@Import(GenreDbStorage.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class GenreDbStorageTest {

  private final GenreDbStorage genreStorage;

  @Test
  void findAllReturnsPredefinedGenres() {
    assertThat(genreStorage.findAll()).hasSize(6);
  }

  @Test
  void getByIdReturnsGenre() {
    assertThat(genreStorage.getById(1).getName()).isEqualTo("Комедия");
  }

  @Test
  void getByIdThrowsForMissingGenre() {
    assertThatThrownBy(() -> genreStorage.getById(999))
        .isInstanceOf(NotFoundException.class);
  }
}
