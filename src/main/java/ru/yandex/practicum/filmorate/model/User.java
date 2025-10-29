package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CHANGES:
 * - CHANGE: id: Integer -> Long.
 * - CHANGE: добавлены/уточнены валидации email/login/birthday.
 * - Примечание: name может быть пустым — сервис подставит login (см. UserService.normalize()).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

  // CHANGE: тип Integer -> Long
  private Long id;

  @NotBlank(message = "Email не может быть пустым.")
  @Email(message = "Некорректный формат email.")
  private String email;

  @NotBlank(message = "Логин не может быть пустым.")
  @Pattern(regexp = "^\\S+$", message = "Логин не должен содержать пробелы.")
  private String login;

  private String name;

  @PastOrPresent(message = "Дата рождения не может быть в будущем.")
  private LocalDate birthday;
}
