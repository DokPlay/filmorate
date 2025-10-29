package ru.yandex.practicum.filmorate.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

/**
 * CHANGES:
 * - CHANGE: Map<Integer, Film> -> Map<Long, Film>, idSeq: AtomicInteger -> long (нет конкуренции потоков).
 * - CHANGE: единая бизнес-валидация даты релиза (не раньше 1895-12-28).
 * - CHANGE: безопасное логирование (без дампа всей сущности).
 *
 * FIX2:
 * - nextId(): упрощено до `return ++idSeq;` (рекомендация ревьюера).
 * - Логи create/update/delete оставлены на уровне INFO (ключевые события).
 * - Добавлен alias-метод getAll() на случай вызовов из контроллеров.
 */
@Slf4j
@Service
public class FilmService {

  // CHANGE: вынесена константа самой ранней корректной даты
  private static final LocalDate EARLIEST_DATE = LocalDate.of(1895, 12, 28);

  // CHANGE: ключи теперь Long
  private final Map<Long, Film> films = new HashMap<>();

  // CHANGE: вместо AtomicInteger — простой long-счётчик
  private long idSeq = 0L;

  public List<Film> findAll() {
    // CHANGE: возвращаем типобезопасный List
    return new ArrayList<>(films.values());
  }

  // FIX2: alias, если где-то используется getAll()
  public List<Film> getAll() {
    return findAll();
  }

  public Film getById(final long id) {
    final Film film = films.get(id);
    if (film == null) {
      throw new NotFoundException("Фильм с id=" + id + " не найден.");
    }
    return film;
  }

  public Film create(final Film film) {
    validateBusinessRules(film);
    final long id = nextId(); // CHANGE: генерация long id
    film.setId(id);
    films.put(id, film);
    log.info("Создан фильм id={} name='{}'", id, film.getName()); // CHANGE: безопасный лог
    return film;
  }

  public Film update(final Film film) {
    if (film.getId() == null || !films.containsKey(film.getId())) {
      throw new NotFoundException("Фильм с id=" + film.getId() + " не найден.");
    }
    validateBusinessRules(film);
    films.put(film.getId(), film);
    log.info("Обновлён фильм id={} name='{}'", film.getId(), film.getName()); // CHANGE
    return film;
  }

  public void delete(final long id) {
    if (!films.containsKey(id)) {
      throw new NotFoundException("Фильм с id=" + id + " не найден.");
    }
    films.remove(id);
    log.info("Удалён фильм id={}", id); // CHANGE
  }

  // CHANGE: инкремент long-последовательности
  // FIX2: упрощённая форма
  private long nextId() {
    return ++idSeq;
  }

  // CHANGE: централизованная бизнес-валидация
  private void validateBusinessRules(final Film film) {
    if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(EARLIEST_DATE)) {
      throw new ValidationException(
          "Дата релиза не может быть раньше " + EARLIEST_DATE + ".");
    }
  }
}
