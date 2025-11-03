package ru.yandex.practicum.filmorate.storage.film;

import java.util.List;
import ru.yandex.practicum.filmorate.model.Film;

/**
 * CHANGES:
 * - SPRINT 11: добавлен интерфейс слоя хранения фильмов.
 *
 * SPRINT 11 (checkstyle):
 * - добавлены пустые строки между методами (EmptyLineSeparator).
 */
public interface FilmStorage {

  List<Film> findAll();

  Film getById(long id);

  Film create(Film film);

  Film update(Film film);

  void delete(long id);
}
