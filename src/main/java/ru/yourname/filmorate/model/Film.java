package ru.yourname.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Film {
  private Integer id;

  @NotBlank(message = "Название фильма не может быть пустым.")
  private String name;

  @Size(max = 200, message = "Максимальная длина описания — 200 символов.")
  private String description;

  @NotNull(message = "Дата релиза обязательна.")
  @PastOrPresent(message = "Дата релиза не может быть в будущем.")
  private LocalDate releaseDate;

  @NotNull(message = "Продолжительность обязательна.")
  @Positive(message = "Продолжительность должна быть положительным числом.")
  private Integer duration; // в минутах
}
