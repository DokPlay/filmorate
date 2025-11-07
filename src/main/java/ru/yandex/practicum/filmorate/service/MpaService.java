package ru.yandex.practicum.filmorate.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

/** Service facade for MPA ratings. */
@Service
@RequiredArgsConstructor
public class MpaService {

  private final MpaStorage mpaStorage;

  public List<Mpa> findAll() {
    return mpaStorage.findAll();
  }

  public Mpa getById(int id) {
    return mpaStorage.getById(id);
  }
}
