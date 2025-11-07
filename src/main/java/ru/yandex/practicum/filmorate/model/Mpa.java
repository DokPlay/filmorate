package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Motion Picture Association rating. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Mpa {

  private Integer id;

  private String name;
}
