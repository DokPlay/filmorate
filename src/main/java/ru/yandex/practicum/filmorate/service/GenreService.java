package ru.yandex.practicum.filmorate.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

/** Service facade for working with genres. */
@Service
@RequiredArgsConstructor
public class GenreService {

  private final GenreStorage genreStorage;

  public List<Genre> findAll() {
    return genreStorage.findAll();
  }

  public Genre getById(int id) {
    return genreStorage.getById(id);
  }
}
