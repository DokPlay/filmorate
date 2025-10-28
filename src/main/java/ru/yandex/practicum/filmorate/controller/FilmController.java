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
 */
@Slf4j
@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {

  private final FilmService filmService;

  @PostMapping
  public Film create(@Valid @RequestBody Film film) {
    // CHANGE: логируем только безопасные поля
    log.debug("POST /films — создание: name='{}'", film.getName());
    return filmService.create(film);
  }

  @PutMapping
  public Film update(@Valid @RequestBody Film film) {
    // CHANGE: не логируем всю сущность
    log.debug("PUT /films — обновление id={}", film.getId());
    return filmService.update(film);
  }

  @GetMapping
  public List<Film> findAll() {
    log.debug("GET /films — список");
    return filmService.findAll();
  }

  @GetMapping("/{id}")
  public Film getById(@PathVariable long id) { // CHANGE: long id
    log.debug("GET /films/{} — получить фильм", id);
    return filmService.getById(id);
  }
}
