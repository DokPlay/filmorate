## Sprint 11 – Database Design (intermediate peer-review task)

### Goal

Design a relational database schema for the existing Filmorate backend so that:
- data is stored persistently in a DB instead of in-memory only,
- the schema supports the current business logic,
- the schema is normalized (up to 3NF),
- it is easy to query films, users, likes, friendships, and popular films.

> Note: in this intermediate task the application code may still use only in-memory storages.
> JDBC integration will be done in the next sprint.

### Domain model extensions

**Film**

- A film can have multiple genres.
- Film must have an MPA rating (age restriction).
- MPA rating values: `G`, `PG`, `PG-13`, `R`, `NC-17`.

**User / Friendship**

- Friendship between two users has a status:
  - `PENDING` – request sent, not confirmed yet,
  - `CONFIRMED` – both users accepted the friendship.

### Functional requirements for the DB schema

The schema must allow to:

1. Store users and films with all required attributes.
2. Store film likes from users and calculate:
   - total likes per film,
   - **top N most popular films** ordered by likes.
3. Store friendships between users with a status and query:
   - the list of a user's friends,
   - mutual (common) friends for two users.
4. Represent film genres and MPA ratings in a normalized way (no arrays, no comma-separated strings).
5. Keep all non-key attributes fully functionally dependent only on the primary key of their table (3NF).

### Deliverables

1. **ER diagram** of the database:
   - export it as an image (PNG/JPEG),
   - add the image to the repository (e.g. `db/filmorate-db.png`),
   - reference it from `db-spec-sprint11.md`.

2. **DDL / pseudo-DDL**:
   - SQL `CREATE TABLE` statements for all tables  
     (or DBML code if you use dbdiagram.io),
   - primary keys, foreign keys and basic constraints.

3. **Examples of SQL queries** in `db-spec-sprint11.md`:
   - get all films,
   - get top N popular films,
   - get a user's friends,
   - get mutual friends of two users.

### Example SQL queries

**Get all films with MPA rating**

## Примеры SQL-запросов для README

```sql
SELECT f.film_id,
       f.name,
       f.description,
       f.release_date,
       f.duration,
       r.code AS mpa_rating
FROM films f
JOIN mpa_ratings r ON r.id = f.mpa_rating_id;

## Get top N popular films by likes

SELECT f.*
FROM films f
LEFT JOIN film_likes l ON l.film_id = f.film_id
GROUP BY f.film_id
ORDER BY COUNT(l.user_id) DESC
LIMIT :count;

## Get user friends (confirmed only)

SELECT u.*
FROM friendships fr
JOIN users u ON u.user_id = fr.friend_id
JOIN friendship_statuses s ON s.id = fr.status_id
WHERE fr.user_id = :user_id
  AND s.code = 'CONFIRMED';

## Get mutual friends of two users

SELECT u.*
FROM friendships f1
JOIN friendships f2 ON f1.friend_id = f2.friend_id
JOIN friendship_statuses s1 ON s1.id = f1.status_id
JOIN friendship_statuses s2 ON s2.id = f2.status_id
JOIN users u ON u.user_id = f1.friend_id
WHERE f1.user_id = :user_id
  AND f2.user_id = :other_user_id
  AND s1.code = 'CONFIRMED'
  AND s2.code = 'CONFIRMED';


