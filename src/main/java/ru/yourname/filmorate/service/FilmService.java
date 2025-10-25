package ru.yourname.filmorate.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yourname.filmorate.exception.NotFoundException;
import ru.yourname.filmorate.exception.ValidationException;
import ru.yourname.filmorate.model.Film;

@Slf4j
@Service
public class FilmService {
  private static final LocalDate EARLIEST_DATE = LocalDate.of(1895, 12, 28);

  private final Map<Integer, Film> films = new HashMap<>();
  private final AtomicInteger idSeq = new AtomicInteger(0);

  public Film add(Film film) {
    validateBusinessRules(film);
    int id = idSeq.incrementAndGet();
    film.setId(id);
    films.put(id, film);
    log.info("Добавлен фильм: {}", film);
    return film;
  }

  public Film update(Film film) {
    if (film.getId() == null || !films.containsKey(film.getId())) {
      throw new NotFoundException("Фильм с id=" + film.getId() + " не найден.");
    }
    validateBusinessRules(film);
    films.put(film.getId(), film);
    log.info("Обновлён фильм: {}", film);
    return film;
  }

  public List<Film> getAll() {
    return new ArrayList<>(films.values());
  }

  private void validateBusinessRules(Film film) {
    if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(EARLIEST_DATE)) {
      throw new ValidationException(
          "Дата релиза не может быть раньше 28.12.1895 (передано: " + film.getReleaseDate() + ").");
    }
  }
}
