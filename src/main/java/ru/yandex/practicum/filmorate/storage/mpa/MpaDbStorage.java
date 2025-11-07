package ru.yandex.practicum.filmorate.storage.mpa;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;

/** JDBC-based implementation of {@link MpaStorage}. */
@Repository
@RequiredArgsConstructor
public class MpaDbStorage implements MpaStorage {

  private static final RowMapper<Mpa> MPA_MAPPER = (rs, rowNum) ->
      new Mpa(rs.getInt("id"), rs.getString("name"));

  private final JdbcTemplate jdbcTemplate;

  @Override
  public List<Mpa> findAll() {
    final String sql = "SELECT id, name FROM mpa_ratings ORDER BY id";
    return jdbcTemplate.query(sql, MPA_MAPPER);
  }

  @Override
  public Mpa getById(int id) {
    final String sql = "SELECT id, name FROM mpa_ratings WHERE id = ?";
    final List<Mpa> ratings = jdbcTemplate.query(sql, MPA_MAPPER, id);
    if (ratings.isEmpty()) {
      throw new NotFoundException("Рейтинг с id=" + id + " не найден.");
    }
    return ratings.get(0);
  }
}
