package ru.yandex.practicum.filmorate.storage.genre;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

/** JDBC-based implementation of {@link GenreStorage}. */
@Repository
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {

  private static final RowMapper<Genre> GENRE_MAPPER = (rs, rowNum) ->
      new Genre(rs.getInt("id"), rs.getString("name"));

  private final JdbcTemplate jdbcTemplate;

  @Override
  public List<Genre> findAll() {
    final String sql = "SELECT id, name FROM genres ORDER BY id";
    return jdbcTemplate.query(sql, GENRE_MAPPER);
  }

  @Override
  public Genre getById(int id) {
    final String sql = "SELECT id, name FROM genres WHERE id = ?";
    final List<Genre> genres = jdbcTemplate.query(sql, GENRE_MAPPER, id);
    if (genres.isEmpty()) {
      throw new NotFoundException("Жанр с id=" + id + " не найден.");
    }
    return genres.get(0);
  }
}
