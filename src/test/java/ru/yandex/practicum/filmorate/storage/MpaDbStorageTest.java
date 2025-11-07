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
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;

@JdbcTest
@AutoConfigureTestDatabase
@Import(MpaDbStorage.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class MpaDbStorageTest {

  private final MpaDbStorage mpaStorage;

  @Test
  void findAllReturnsFiveRatings() {
    assertThat(mpaStorage.findAll()).hasSize(5);
  }

  @Test
  void getByIdReturnsRating() {
    assertThat(mpaStorage.getById(1).getName()).isEqualTo("G");
  }

  @Test
  void getByIdThrowsForMissingRating() {
    assertThatThrownBy(() -> mpaStorage.getById(999))
        .isInstanceOf(NotFoundException.class);
  }
}
