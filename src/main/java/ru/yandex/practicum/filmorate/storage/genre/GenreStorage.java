package ru.yandex.practicum.filmorate.storage.genre;

import java.util.List;
import ru.yandex.practicum.filmorate.model.Genre;

/** Storage abstraction for film genres. */
public interface GenreStorage {

  List<Genre> findAll();

  Genre getById(int id);
}
