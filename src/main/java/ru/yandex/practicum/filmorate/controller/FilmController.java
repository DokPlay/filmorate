package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

/**
 * CHANGES:
 * - CHANGE: безопасное логирование — без дампа всей сущности.
 * - CHANGE: согласование типов с Long id.
 *
 * FIX2:
 * - INFO для ключевых событий (create/update/getById) с краткими параметрами.
 * - DEBUG для детальных сведений (полный payload), включается при необходимости.
 */
@Slf4j
@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {

  private final FilmService filmService;

  @PostMapping
  public Film create(@Valid @RequestBody final Film film) {
    // CHANGE: логируем только безопасные поля
    // FIX2: основное событие — на уровне INFO
    log.info("POST /films — create name='{}'", film.getName());
    // FIX2: деталь — полный payload в DEBUG
    log.debug("POST /films payload: {}", film);
    return filmService.create(film);
  }

  @PutMapping
  public Film update(@Valid @RequestBody final Film film) {
    // CHANGE: не логируем всю сущность
    // FIX2: важное событие — на уровне INFO + краткий контекст
    log.info("PUT /films — update id={} name='{}'", film.getId(), film.getName());
    // FIX2: полный payload — в DEBUG
    log.debug("PUT /films payload: {}", film);
    return filmService.update(film);
  }

  @GetMapping
  public List<Film> findAll() {
    // FIX2: список — обычная операция, оставляем в DEBUG, чтобы не шумело в INFO
    log.debug("GET /films — list");
    return filmService.findAll();
  }

  @GetMapping("/{id}")
  public Film getById(@PathVariable final long id) { // CHANGE: long id
    // FIX2: входной параметр важен — логируем на INFO
    log.info("GET /films/{} — fetch", id);
    return filmService.getById(id);
  }
}
