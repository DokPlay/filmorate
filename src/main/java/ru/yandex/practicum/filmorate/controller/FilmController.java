package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
// TODO сортировка/лимит теперь в хранилище.
/**
 * CHANGES:
 * - CHANGE: безопасное логирование — без дампа всей сущности.
 * - CHANGE: согласование типов с Long id.
 *
 * FIX2:
 * - INFO для ключевых событий (create/update/getById) с краткими параметрами.
 * - DEBUG для детальных сведений (полный payload), включается при необходимости.
 *
 * SPRINT 11:
 * - добавлены REST-эндпоинты лайков и популярных фильмов:
 *   PUT    /films/{id}/like/{userId}      — поставить лайк фильму
 *   DELETE /films/{id}/like/{userId}      — убрать лайк
 *   GET    /films/popular?count={count}   — топ популярных фильмов (по умолчанию 10)
 */
@Slf4j
@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {

  private final FilmService filmService;

  @PostMapping
  public Film create(@Valid @RequestBody final Film film) {
    // CHANGE/FIX2: ключевое событие — INFO, без дампа сущности
    log.info("POST /films — create name='{}'", film.getName());
    // FIX2: полный payload — в DEBUG (включайте при отладке)
    log.debug("POST /films payload: {}", film);
    return filmService.create(film);
  }

  @PutMapping
  public Film update(@Valid @RequestBody final Film film) {
    // CHANGE/FIX2: важное событие — INFO + краткий контекст
    log.info("PUT /films — update id={} name='{}'", film.getId(), film.getName());
    // FIX2: полный payload — в DEBUG
    log.debug("PUT /films payload: {}", film);
    return filmService.update(film);
  }

  @GetMapping
  public List<Film> findAll() {
    // FIX2: список — обычная операция, логируем в DEBUG
    log.debug("GET /films — list");
    return filmService.findAll();
  }

  @GetMapping("/{id}")
  public Film getById(@PathVariable final long id) { // CHANGE: long id
    // FIX2: входной параметр важен — логируем на INFO
    log.info("GET /films/{} — fetch", id);
    return filmService.getById(id);
  }

  // ----------- SPRINT 11: лайки и популярность -----------

  @PutMapping("/{id}/like/{userId}")
  public void addLike(@PathVariable final long id, @PathVariable final long userId) {
    log.info("PUT /films/{}/like/{} — add like", id, userId);
    filmService.addLike(id, userId);
  }

  @DeleteMapping("/{id}/like/{userId}")
  public void removeLike(@PathVariable final long id, @PathVariable final long userId) {
    log.info("DELETE /films/{}/like/{} — remove like", id, userId);
    filmService.removeLike(id, userId);
  }

  @GetMapping("/popular")
  public List<Film> popular(@RequestParam(name = "count", defaultValue = "10") final int count) {
    log.debug("GET /films/popular?count={} — list", count);
    return filmService.getPopular(count);
  }
}
