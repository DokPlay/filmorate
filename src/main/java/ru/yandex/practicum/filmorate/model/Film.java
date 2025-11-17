package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CHANGES:
 * - CHANGE: id: Integer -> Long (во избежание переполнения и единообразия по проекту).
 * - CHANGE: уточнены/добавлены валидационные аннотации и человекочитаемые сообщения.
 *
 * SPRINT 11:
 * - добавлено поле likes (Set<Long>) для лайков фильма пользователями.
 * - likes инициализируется пустым Set для уникальности и отсутствия NPE.
 * - убран @AllArgsConstructor из-за final-поля likes (иначе конструктор станет неудобным).
 */
@Data
@NoArgsConstructor
public class Film {

  // CHANGE: тип Integer -> Long
  private Long id;

  @NotBlank(message = "Название фильма не может быть пустым.")
  private String name;

  // CHANGE: ограничение длины описания в соответствии с требованиями курса
  @Size(max = 200, message = "Максимальная длина описания — 200 символов.")
  private String description;

  @NotNull(message = "Дата релиза обязательна.")
  @PastOrPresent(message = "Дата релиза не может быть в будущем.")
  private LocalDate releaseDate;

  @NotNull(message = "Продолжительность обязательна.")
  @Positive(message = "Продолжительность должна быть положительным числом.")
  private Integer duration;

  @NotNull(message = "Рейтинг обязателен.")
  private Mpa mpa;

  private Set<Genre> genres = new LinkedHashSet<>();

  // SPRINT 11: лайки от пользователей (уникальные id)
  private final Set<Long> likes = new HashSet<>();

  public void setGenres(Set<Genre> genres) {
    this.genres = genres == null ? new LinkedHashSet<>() : new LinkedHashSet<>(genres);
  }
}
