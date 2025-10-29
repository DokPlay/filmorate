package ru.yandex.practicum.filmorate.storage.film;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

/**
 * SPRINT 11:
 * - перенос хранения фильмов из сервиса в компонент-хранилище.
 * - генерация id внутри хранилища.
 */
@Component
public class InMemoryFilmStorage implements FilmStorage {

  private final Map<Long, Film> films = new HashMap<>();
  private long idSeq = 0L;

  @Override
  public List<Film> findAll() {
    return new ArrayList<>(films.values());
  }

  @Override
  public Film getById(long id) {
    Film film = films.get(id);
    if (film == null) {
      throw new NotFoundException("Фильм с id=" + id + " не найден.");
    }
    return film;
  }

  @Override
  public Film create(Film film) {
    film.setId(++idSeq);              // SPRINT 11
    films.put(film.getId(), film);
    return film;
  }

  @Override
  public Film update(Film film) {
    if (film.getId() == null || !films.containsKey(film.getId())) {
      throw new NotFoundException("Фильм с id=" + film.getId() + " не найден.");
    }
    films.put(film.getId(), film);
    return film;
  }

  @Override
  public void delete(long id) {
    if (!films.containsKey(id)) {
      throw new NotFoundException("Фильм с id=" + id + " не найден.");
    }
    films.remove(id);
  }
}
