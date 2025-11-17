package ru.yandex.practicum.filmorate.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.GenreService;

/** REST controller for film genres. */
@Slf4j
@RestController
@RequestMapping("/genres")
@RequiredArgsConstructor
public class GenreController {

  private final GenreService genreService;

  @GetMapping
  public List<Genre> findAll() {
    log.debug("GET /genres — list");
    return genreService.findAll();
  }

  @GetMapping("/{id}")
  public Genre getById(@PathVariable final int id) {
    log.info("GET /genres/{} — fetch", id);
    return genreService.getById(id);
  }
}
