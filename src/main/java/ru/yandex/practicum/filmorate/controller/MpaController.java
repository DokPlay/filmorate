package ru.yandex.practicum.filmorate.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.MpaService;

/** REST controller for MPA ratings. */
@Slf4j
@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
public class MpaController {

  private final MpaService mpaService;

  @GetMapping
  public List<Mpa> findAll() {
    log.debug("GET /mpa — list");
    return mpaService.findAll();
  }

  @GetMapping("/{id}")
  public Mpa getById(@PathVariable final int id) {
    log.info("GET /mpa/{} — fetch", id);
    return mpaService.getById(id);
  }
}
