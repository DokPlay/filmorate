package ru.yourname.filmorate.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yourname.filmorate.model.Film;
import ru.yourname.filmorate.service.FilmService;

@Slf4j
@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {

  private final FilmService filmService;

  @PostMapping
  public Film create(@Valid @RequestBody Film film) {
    log.debug("POST /films payload: {}", film);
    return filmService.add(film);
  }

  @PutMapping
  public Film update(@Valid @RequestBody Film film) {
    log.debug("PUT /films payload: {}", film);
    return filmService.update(film);
  }

  @GetMapping
  public List<Film> getAll() {
    return filmService.getAll();
  }
}
