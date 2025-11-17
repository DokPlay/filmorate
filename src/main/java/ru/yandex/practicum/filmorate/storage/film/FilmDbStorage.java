package ru.yandex.practicum.filmorate.storage.film;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

/** JDBC implementation of {@link FilmStorage}. */
@Repository
@Primary
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

  private static final String FILM_SELECT = "SELECT f.id, f.name, f.description, f.release_date, "
      + "f.duration, f.mpa_id, m.name AS mpa_name FROM films f JOIN mpa_ratings m ON f.mpa_id = m.id ";

  private static final RowMapper<Film> FILM_MAPPER = (rs, rowNum) -> {
    final Film film = new Film();
    film.setId(rs.getLong("id"));
    film.setName(rs.getString("name"));
    film.setDescription(rs.getString("description"));
    final Date releaseDate = rs.getDate("release_date");
    if (releaseDate != null) {
      film.setReleaseDate(releaseDate.toLocalDate());
    }
    film.setDuration(rs.getInt("duration"));
    film.setMpa(new Mpa(rs.getInt("mpa_id"), rs.getString("mpa_name")));
    return film;
  };

  private final JdbcTemplate jdbcTemplate;

  @Override
  public List<Film> findAll() {
    final List<Film> films = jdbcTemplate.query(FILM_SELECT + "ORDER BY f.id", FILM_MAPPER);
    enrichFilms(films);
    return films;
  }

  @Override
  public Film getById(long id) {
    final List<Film> films = jdbcTemplate.query(FILM_SELECT + "WHERE f.id = ?", FILM_MAPPER, id);
    if (films.isEmpty()) {
      throw new NotFoundException("Фильм с id=" + id + " не найден.");
    }
    enrichFilms(films);
    return films.get(0);
  }

  @Override
  public Film create(Film film) {
    final SimpleJdbcInsert insert = new SimpleJdbcInsert(jdbcTemplate)
        .withTableName("films")
        .usingGeneratedKeyColumns("id");
    final Map<String, Object> values = new LinkedHashMap<>(5);
    values.put("name", film.getName());
    values.put("description", film.getDescription());
    values.put("release_date",
        film.getReleaseDate() == null ? null : Date.valueOf(film.getReleaseDate()));
    values.put("duration", film.getDuration());
    values.put("mpa_id", film.getMpa().getId());
    final Number id = insert.executeAndReturnKey(values);
    film.setId(id.longValue());
    updateGenres(film);
    updateLikes(film);
    return getById(film.getId());
  }

  @Override
  public Film update(Film film) {
    final String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, "
        + "mpa_id = ? WHERE id = ?";
    final int updated = jdbcTemplate.update(sql,
        film.getName(),
        film.getDescription(),
        film.getReleaseDate() == null ? null : Date.valueOf(film.getReleaseDate()),
        film.getDuration(),
        film.getMpa().getId(),
        film.getId());
    if (updated == 0) {
      throw new NotFoundException("Фильм с id=" + film.getId() + " не найден.");
    }
    updateGenres(film);
    updateLikes(film);
    return getById(film.getId());
  }

  @Override
  public void delete(long id) {
    final int deleted = jdbcTemplate.update("DELETE FROM films WHERE id = ?", id);
    if (deleted == 0) {
      throw new NotFoundException("Фильм с id=" + id + " не найден.");
    }
  }

  @Override
  public List<Film> findMostPopular(int limit) {
    final int normalizedLimit =
        limit <= 0 ? FilmStorage.DEFAULT_POPULAR_LIMIT : limit;
    final String sql = FILM_SELECT
        + "LEFT JOIN (SELECT film_id, COUNT(*) AS likes_count FROM film_likes GROUP BY film_id) fl "
        + "ON f.id = fl.film_id "
        + "ORDER BY COALESCE(fl.likes_count, 0) DESC, f.id ASC LIMIT ?";
    final List<Film> films = jdbcTemplate.query(sql, FILM_MAPPER, normalizedLimit);
    enrichFilms(films);
    return films;
  }

  private void enrichFilms(List<Film> films) {
    if (films.isEmpty()) {
      return;
    }
    final List<Long> filmIds = films.stream()
        .map(Film::getId)
        .distinct()
        .toList();
    final Map<Long, Set<Genre>> genresByFilm = loadGenres(filmIds);
    final Map<Long, Set<Long>> likesByFilm = loadLikes(filmIds);
    for (Film film : films) {
      final long filmId = film.getId();
      film.setGenres(genresByFilm.getOrDefault(filmId, Collections.emptySet()));

      film.getLikes().clear();
      film.getLikes().addAll(likesByFilm.getOrDefault(filmId, Collections.emptySet()));
    }
  }

  private Map<Long, Set<Genre>> loadGenres(List<Long> filmIds) {
    if (filmIds.isEmpty()) {
      return Collections.emptyMap();
    }
    final String placeholders = buildPlaceholders(filmIds.size());
    final String sql =
        "SELECT fg.film_id, g.id, g.name FROM film_genres fg "
            + "JOIN genres g ON fg.genre_id = g.id "
            + "WHERE fg.film_id IN (" + placeholders + ") ORDER BY fg.film_id, g.id";
    return jdbcTemplate.query(sql, this::extractGenres, filmIds.toArray(Object[]::new));
  }

  private Map<Long, Set<Long>> loadLikes(List<Long> filmIds) {
    if (filmIds.isEmpty()) {
      return Collections.emptyMap();
    }
    final String placeholders = buildPlaceholders(filmIds.size());
    final String sql = "SELECT film_id, user_id FROM film_likes WHERE film_id IN ("
        + placeholders + ") ORDER BY film_id, user_id";
    return jdbcTemplate.query(sql, this::extractLikes, filmIds.toArray(Object[]::new));
  }

  private Map<Long, Set<Genre>> extractGenres(ResultSet rs) throws SQLException {
    final Map<Long, Set<Genre>> result = new LinkedHashMap<>();
    while (rs.next()) {
      final long filmId = rs.getLong("film_id");
      result.computeIfAbsent(filmId, key -> new LinkedHashSet<>())
          .add(new Genre(rs.getInt("id"), rs.getString("name")));
    }
    return result;
  }

  private Map<Long, Set<Long>> extractLikes(ResultSet rs) throws SQLException {
    final Map<Long, Set<Long>> result = new LinkedHashMap<>();
    while (rs.next()) {
      final long filmId = rs.getLong("film_id");
      result.computeIfAbsent(filmId, key -> new LinkedHashSet<>())
          .add(rs.getLong("user_id"));
    }
    return result;
  }

  private String buildPlaceholders(int size) {
    return String.join(", ", Collections.nCopies(size, "?"));
  }

  private void updateGenres(Film film) {
    jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());
    final Set<Genre> genres = film.getGenres();
    if (genres == null || genres.isEmpty()) {
      return;
    }
    final int expectedSize = Math.max(genres.size(), 1);
    final Set<Integer> processed = new HashSet<>(expectedSize);
    final List<Object[]> batchArgs = new ArrayList<>(expectedSize);
    final String sql =
        "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
    for (Genre genre : genres) {
      if (genre == null || genre.getId() == null || !processed.add(genre.getId())) {
        continue;
      }
      batchArgs.add(new Object[]{film.getId(), genre.getId()});
    }
    if (!batchArgs.isEmpty()) {
      jdbcTemplate.batchUpdate(sql, batchArgs);
    }
  }

  private void updateLikes(Film film) {
    jdbcTemplate.update("DELETE FROM film_likes WHERE film_id = ?", film.getId());
    final Set<Long> likes = film.getLikes();
    if (likes == null || likes.isEmpty()) {
      return;
    }
    final int expectedSize = Math.max(likes.size(), 1);
    final Set<Long> processed = new HashSet<>(expectedSize);
    final List<Object[]> batchArgs = new ArrayList<>(expectedSize);
    final String sql =
        "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
    for (Long userId : likes) {
      if (userId == null || !processed.add(userId)) {
        continue;
      }
      batchArgs.add(new Object[]{film.getId(), userId});
    }
    if (!batchArgs.isEmpty()) {
      jdbcTemplate.batchUpdate(sql, batchArgs);
    }
  }
}
