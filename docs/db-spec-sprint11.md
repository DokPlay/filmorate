# Sprint 11 – Database design (intermediate peer-review task)

## ER-диаграмма

Схема описывает реляционную БД (до 3НФ) для текущего бэкенда Filmorate и ближайших фич:

* постоянное хранение пользователей и фильмов;
* лайки фильмов и выбор “top N популярных фильмов”;
* дружба между пользователями со статусом;
* MPA-рейтинг и несколько жанров на фильм.

## Доменные расширения

### Film

* У фильма может быть несколько жанров.
* У фильма **обязателен** MPA-рейтинг (возрастное ограничение).
* Допустимые коды MPA: `G`, `PG`, `PG-13`, `R`, `NC-17` (хранятся в `mpa_ratings.name`).

Реализация:

* справочник `genres` и связующая таблица `film_genres (film_id, genre_id)`;
* справочник `mpa_ratings (id, name)`; внешний ключ `films.rating_mpa_id → mpa_ratings.id`.

### User / Friendship

* Дружба моделируется **направленными** записями в `friendships`:

  * когда пользователь A отправляет запрос пользователю B — добавляется строка `(A, B, 'unconfirmed')`;
  * когда пользователь B отправляет ответный запрос A — добавляется строка `(B, A, 'unconfirmed')`, после чего обе записи переводятся в статус `'confirmed'`.
* Статусы задаются enum’ом:

```dbml
Enum FriendStatus {
  unconfirmed
  confirmed
}
```

> Инвариант: подтверждённая дружба = **наличие двух встречных записей** `(A,B)` и `(B,A)`; поле `status` должно быть `'confirmed'` у обеих. Для вычисления статуса можно опираться либо на поле `status`, либо (надёжнее) на наличие пары взаимных строк.

## Состав таблиц

* `users(id, email, login, name, birthday)` — пользователи; `email` и `login` уникальны.
* `films(id, name, description, release_date, duration, rating_mpa_id)` — фильмы; FK на `mpa_ratings`.
* `mpa_ratings(id, name)` — справочник MPA (значения: `G`, `PG`, `PG-13`, `R`, `NC-17`).
* `genres(id, name)` — справочник жанров.
* `film_genres(film_id, genre_id)` — связь “многие-ко-многим” фильмов и жанров.
* `likes(user_id, film_id)` — лайки фильмов пользователями (композитный PK).
* `friendships(user_id, friend_id, status)` — дружба (направленные ребра, композитный PK).

Все неключевые атрибуты полностью функционально зависят только от первичных ключей своих таблиц. Связи многие-ко-многим вынесены в отдельные таблицы-перекрёстки.

## Примеры SQL-запросов

### Получить все фильмы с MPA-рейтингом

```sql
SELECT
  f.id,
  f.name,
  f.description,
  f.release_date,
  f.duration,
  r.name AS mpa_rating
FROM films AS f
JOIN mpa_ratings AS r ON r.id = f.rating_mpa_id;
```

### Топ-N популярных фильмов по количеству лайков

```sql
SELECT
  f.id,
  f.name,
  COUNT(l.user_id) AS like_count
FROM films AS f
LEFT JOIN likes AS l ON l.film_id = f.id
GROUP BY f.id, f.name
ORDER BY like_count DESC, f.id
LIMIT :count;
```

### Друзья пользователя (только подтверждённые)

Вариант по полю `status`:

```sql
SELECT u.*
FROM friendships AS fr
JOIN users AS u ON u.id = fr.friend_id
WHERE fr.user_id = :user_id
  AND fr.status = 'confirmed';
```

Надёжный вариант (по взаимному наличию парных записей), не полагаясь на `status`:

```sql
SELECT u.*
FROM friendships AS f1
JOIN friendships AS f2
  ON f2.user_id = f1.friend_id
 AND f2.friend_id = f1.user_id
JOIN users AS u ON u.id = f1.friend_id
WHERE f1.user_id = :user_id;
```

### Взаимные друзья двух пользователей

```sql
SELECT u.*
FROM friendships AS a1
JOIN friendships AS a2
  ON a2.user_id = a1.friend_id
 AND a2.friend_id = a1.user_id
JOIN friendships AS b1
  ON b1.user_id = :other_user_id
 AND b1.friend_id = a1.friend_id
JOIN friendships AS b2
  ON b2.user_id = a1.friend_id
 AND b2.friend_id = :other_user_id
JOIN users AS u ON u.id = a1.friend_id
WHERE a1.user_id = :user_id
  -- опционально требуем статус 'confirmed' у всех четырёх рёбер
  AND a1.status = 'confirmed'
  AND a2.status = 'confirmed'
  AND b1.status = 'confirmed'
  AND b2.status = 'confirmed';
```

### Исходящие / входящие запросы в друзья (ожидают подтверждения)

```sql
-- Исходящие: я добавил, встречной записи нет
SELECT f1.friend_id AS pending_outgoing_to
FROM friendships AS f1
LEFT JOIN friendships AS f2
  ON f2.user_id = f1.friend_id
 AND f2.friend_id = f1.user_id
WHERE f1.user_id = :user_id
  AND (f1.status = 'unconfirmed' OR f1.status IS NULL)
  AND f2.user_id IS NULL;

-- Входящие: меня добавили, моей встречной записи нет
SELECT f1.user_id AS pending_incoming_from
FROM friendships AS f1
LEFT JOIN friendships AS f2
  ON f2.user_id = f1.friend_id
 AND f2.friend_id = f1.user_id
WHERE f1.friend_id = :user_id
  AND (f1.status = 'unconfirmed' OR f1.status IS NULL)
  AND f2.user_id IS NULL;
```

### Сводка статусов по парам (вычисляем «on-the-fly»)

```sql
-- Работает в Postgres/MySQL 8+: считаем ребра в неупорядоченной паре
SELECT
  LEAST(user_id, friend_id)   AS u1,
  GREATEST(user_id, friend_id) AS u2,
  COUNT(*) AS edges, -- 1=односторонне, 2=взаимно
  CASE WHEN COUNT(*) = 2 THEN 'confirmed' ELSE 'unconfirmed' END AS derived_status
FROM friendships
GROUP BY LEAST(user_id, friend_id), GREATEST(user_id, friend_id);
```

(Опционально можно оформить как `VIEW friend_pairs` и использовать в отчётах.)

## Замечания по качеству данных и ограничениям

* `users.email` и `users.login` — уникальные.
* `friendships` — составной PK `(user_id, friend_id)` запрещает дубликаты направлений.
* Самодружба (`user_id = friend_id`) должна блокироваться на уровне приложения или CHECK-ограничением (если поддерживается вашей СУБД).
* Рекомендуется валидировать `films.duration > 0` на уровне приложения (или через CHECK, если это не ломает совместимость).
