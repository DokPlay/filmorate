package ru.yandex.practicum.filmorate.service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
// CHANGES/SPRINT 11 FIX3: удалён неиспользуемый импорт NotFoundException,
// он больше не упоминается напрямую в этом классе.
// import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

/**
 * CHANGES:
 * - CHANGE: Map -> Map, idSeq: AtomicInteger -> long (нет конкуренции потоков).
 * - CHANGE: единая бизнес-валидация даты релиза (не раньше 1895-12-28).
 * - CHANGE: безопасное логирование (без дампа всей сущности).
 *
 * FIX2:
 * - nextId(): упрощено до {@code return ++idSeq;} (рекомендация ревьюера).
 * - Логи create/update/delete оставлены на уровне INFO (ключевые события).
 * - Добавлен alias-метод getAll() на случай вызовов из контроллеров.
 *
 * SPRINT 11:
 * - сервис зависит от интерфейсов хранилищ (DI через конструктор) — {@link FilmStorage}, {@link UserStorage}.
 * - добавлены операции лайков и получения популярных фильмов.
 * - генерация id и хранение полностью перенесены в InMemoryFilmStorage; локальные Map/счётчик удалены.
 * - метод getAll() сохранён как алиас findAll() для обратной совместимости.
 *
 * SPRINT 11 FIX:
 * - Перенос сортировки/лимита популярных фильмов в слой хранения (см. FilmStorage.findMostPopular).
 * - Уровень лога при неуспешном удалении лайка повышен до WARN по рекомендации ревью.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {

  // CHANGE: вынесена константа самой ранней корректной даты
  private static final LocalDate EARLIEST_DATE = LocalDate.of(1895, 12, 28);
  private static final int DEFAULT_POPULAR_LIMIT = FilmStorage.DEFAULT_POPULAR_LIMIT;

  // SPRINT 11: внедряем зависимости от интерфейсов хранилищ
  private final FilmStorage filmStore;
  private final UserStorage userStore;

  // CHANGE: возвращаем типобезопасный List (теперь — из хранилища)
  public List<Film> findAll() {
    // NOTE: findAll без лимита потенциально опасен на БД; хранить для обратной совместимости.
    return filmStore.findAll();
  }

  // FIX2: alias, если где-то используется getAll()
  public List<Film> getAll() {
    return findAll();
  }

  public Film getById(final long id) {
    // SPRINT 11: делегируем хранилищу (NotFoundException пробрасывается из него)
    return filmStore.getById(id);
  }

  public Film create(final Film film) {
    validateBusinessRules(film);
    normalizeGenres(film);
    // SPRINT 11: генерация id и сохранение — в storage
    final Film saved = filmStore.create(film);
    // CHANGE: безопасный лог
    log.info("Создан фильм id={} name='{}'", saved.getId(), saved.getName());
    return saved;
  }

  public Film update(final Film film) {
    if (film.getId() == null) {
      throw new ValidationException("id обязателен для обновления фильма.");
    }
    validateBusinessRules(film);
    normalizeGenres(film);
    // SPRINT 11: обновление — через storage
    final Film saved = filmStore.update(film);
    // CHANGE
    log.info("Обновлён фильм id={} name='{}'", saved.getId(), saved.getName());
    return saved;
  }

  public void delete(final long id) {
    // SPRINT 11: удаление — через storage
    filmStore.delete(id);
    // CHANGE
    log.info("Удалён фильм id={}", id);
  }

  // ----------- SPRINT 11: лайки и популярность -----------

  public void addLike(final long filmId, final long userId) {
    final Film film = filmStore.getById(filmId); // NotFound -> 404, если нет фильма
    userStore.getById(userId); // NotFound -> 404, если нет пользователя

    final boolean added = film.getLikes().add(userId);
    if (added) {
      // фиксируем изменение состояния
      filmStore.update(film);
      log.info("Пользователь id={} поставил лайк фильму id={}", userId, filmId);
    } else {
      log.debug("Повторный лайк игнорирован: userId={} filmId={}", userId, filmId);
    }
  }

  public void removeLike(final long filmId, final long userId) {
    final Film film = filmStore.getById(filmId);
    final boolean removed = film.getLikes().remove(userId);
    if (removed) {
      filmStore.update(film);
      log.info("Пользователь id={} удалил лайк фильму id={}", userId, filmId);
    } else {
      // SPRINT 11 FIX: если важно видеть причину, поднимаем уровень до WARN (по ревью)
      log.warn("Удаление лайка не выполнено: лайка не было (userId={} filmId={})", userId, filmId);
    }
  }

  public List<Film> getPopular(int count) {
    final int effectiveLimit = count <= 0 ? DEFAULT_POPULAR_LIMIT : count; // SPRINT 11: дефолт, если параметр не задан/некорректен
    // SPRINT 11 FIX: сортировку и лимит выполняет хранилище (для будущей БД)
    return filmStore.findMostPopular(effectiveLimit);
  }

  // ----------- валидация -----------

  // CHANGE: централизованная бизнес-валидация
  private void validateBusinessRules(final Film film) {
    if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(EARLIEST_DATE)) {
      throw new ValidationException("Дата релиза не может быть раньше " + EARLIEST_DATE + ".");
    }
    if (film.getMpa() == null || film.getMpa().getId() == null) {
      throw new ValidationException("Рейтинг MPA обязателен.");
    }
    if (film.getMpa().getId() <= 0) {
      throw new ValidationException("Некорректный идентификатор рейтинга.");
    }
  }

  private void normalizeGenres(final Film film) {
    if (film.getGenres() == null || film.getGenres().isEmpty()) {
      film.setGenres(Collections.emptySet());
      return;
    }
    final Set<Genre> genres = film.getGenres();
    final int expectedSize = Math.max(genres.size(), 1);
    final Set<Integer> seen = new HashSet<>(expectedSize);
    final Set<Genre> normalized = new LinkedHashSet<>(expectedSize);
    genres.stream()
        .filter(Objects::nonNull)
        .filter(genre -> genre.getId() != null && genre.getId() > 0)
        .sorted(Comparator.comparingInt(Genre::getId))
        .forEach(genre -> {
          if (seen.add(genre.getId())) {
            normalized.add(new Genre(genre.getId(), genre.getName()));
          }
        });
    film.setGenres(normalized);
  }
}
