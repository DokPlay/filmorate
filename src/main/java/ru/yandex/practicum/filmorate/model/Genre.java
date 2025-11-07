package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Genre of a film. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Genre {

  private Integer id;

  private String name;
}
