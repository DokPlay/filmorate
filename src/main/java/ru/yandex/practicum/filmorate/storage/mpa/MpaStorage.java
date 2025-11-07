package ru.yandex.practicum.filmorate.storage.mpa;

import java.util.List;
import ru.yandex.practicum.filmorate.model.Mpa;

/** Storage abstraction for MPA ratings. */
public interface MpaStorage {

  List<Mpa> findAll();

  Mpa getById(int id);
}
