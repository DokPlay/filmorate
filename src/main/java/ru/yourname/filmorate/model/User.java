package ru.yourname.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
  private Integer id;

  @NotBlank(message = "Email не может быть пустым.")
  @Email(message = "Некорректный формат email.")
  private String email;

  @NotBlank(message = "Логин не может быть пустым.")
  @Pattern(regexp = "^\\S+$", message = "Логин не должен содержать пробелы.")
  private String login;

  // Может быть пустым — в таком случае подставим login на стороне сервиса
  private String name;

  @PastOrPresent(message = "Дата рождения не может быть в будущем.")
  private LocalDate birthday;
}
