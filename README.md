

````markdown
# Filmorate

Учебный бэкенд-сервис на **Spring Boot 3.3 / Java 17** для работы с фильмами и пользователями.  
Спринт 10 — REST API и хранение в памяти.  
Спринт 11 — проектирование реляционной базы данных для переноса данных из памяти в БД.

---

## Спринт 10 — REST API, in-memory хранилище

Бэкенд-сервис на Spring Boot 3.3 / Java 17 для работы с фильмами и пользователями.  
Данные пока хранятся в памяти (`Map`).  
Ветка задания: **`controllers-films-users`**.

### Что сделано ✅

**Модели**
- `Film`, `User` (поля по ТЗ).

**REST-контроллеры**
- `FilmController`, `UserController`
- `POST/PUT/GET` для `/films` и `/users`.

**Хранение**
- In-memory через сервисы `FilmService`, `UserService` с авто-генерацией ID.

**Валидация**
- Базовые проверки из ТЗ, глобальный обработчик ошибок `GlobalExceptionHandler`.
- Бизнес-правило: дата релиза фильма не раньше **28.12.1895**.

**Логирование**
- Логируются операции добавления/обновления и причины ошибок (`@Slf4j` от Lombok).

**Тесты**
- Unit-тесты валидации через `MockMvc` (`mvn test` проходит).

### Дополнительное задание ⭐

Подключён `spring-boot-starter-validation`.

Применены аннотации Jakarta Validation:

**Film**
- `@NotBlank` `name`
- `@Size(max = 200)` `description`
- `@NotNull @PastOrPresent` `releaseDate`
- `@NotNull @Positive` `duration`

**User**
- `@NotBlank @Email` `email`
- `@NotBlank @Pattern(^\\S+$)` `login`
- `name` может быть пустым (подставляется `login`)
- `@PastOrPresent` `birthday`

Во всех `POST/PUT` используется `@Valid @RequestBody`.

### Запуск

```bash
# из корня проекта
mvn spring-boot:run

# или
mvn clean package && java -jar target/filmorate-1.0.0.jar
````

Сервис поднимется на `http://localhost:8080`.

### Эндпоинты

```text
POST   /users       — создать пользователя
PUT    /users       — обновить пользователя
GET    /users       — список пользователей

POST   /films       — создать фильм
PUT    /films       — обновить фильм
GET    /films       — список фильмов
```

### Примеры запросов

**POST /users**

```http
POST /users
Content-Type: application/json

{
  "email": "user@example.com",
  "login": "space_cadet",
  "name": "",
  "birthday": "1990-01-01"
}
```

**POST /films**

```http
POST /films
Content-Type: application/json

{
  "name": "Interstellar",
  "description": "A science fiction film.",
  "releaseDate": "2014-11-07",
  "duration": 169
}
```

### Валидация (кратко)

**Film**

* `name` не пустой;
* `description` ≤ 200 символов;
* `releaseDate` не в будущем и ≥ `1895-12-28`;
* `duration` > 0.

**User**

* `email` валидный;
* `login` не пустой и без пробелов;
* `name` может быть пустым — подставится `login`;
* `birthday` не в будущем.

Ошибки → **400** с описанием по полям, отсутствующие ресурсы → **404**.

**Тесты**

```bash
mvn test
```

Покрывают позитивные и граничные кейсы валидации для `/films` и `/users`.

---

## Спринт 11 — Проектирование базы данных

На этом этапе спроектирована реляционная БД, которая повторяет бизнес-логику приложения и готова к переносу данных из памяти в СУБД (PostgreSQL).

### Что сделано ✅

* Добавлены сущности для БД:

  * `users` — пользователи.
  * `films` — фильмы.
  * `mpa_ratings` — рейтинг Ассоциации кинокомпаний (MPA).
  * `genres` — жанры фильмов.
  * `film_genres` — связь «многие ко многим» фильм–жанр.
  * `likes` — лайки фильмов пользователями.
  * `friendships` — связь «дружба» между пользователями со статусом:

    * `unconfirmed` — запрос отправлен;
    * `confirmed` — дружба подтверждена.
* Структура приведена к **третьей нормальной форме (3NF)**:

  * каждый столбец хранит одно значение;
  * нет повторяющихся групп и массивов;
  * все неключевые атрибуты зависят только от первичного ключа.
* Нарисована **ER-диаграмма** в `dbdiagram.io`.
* Подготовлен SQL-скрипт создания таблиц (например, `db/schema.sql`).

### Название диаграммы

В dbdiagram.io можно использовать:

```text
Filmorate DB
Filmorate — ER-диаграмма реляционной базы данных
```

### ER-диаграмма

Картинка лежит в репозитории, например `diagram/filmorate_db_erd.png`:

```markdown
![Filmorate DB ERD](./diagram/filmorate_db_erd.png)
```

### Структура таблиц (кратко)

* **users** — пользователи сервиса.
* **mpa_ratings** — справочник рейтингов MPA (`G`, `PG`, `PG-13`, `R`, `NC-17`).
* **genres** — справочник жанров (комедия, драма, боевик и т.п.).
* **films** — фильмы, ссылаются на `mpa_ratings`.
* **film_genres** — связь фильм ↔ жанр (многие-ко-многим).
* **likes** — лайки фильмов пользователями (user ↔ film).
* **friendships** — дружба между пользователями со статусом подтверждения.

### SQL-схема (основной скрипт)

```sql
CREATE TABLE users (
    id INT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    login VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    birthday DATE NOT NULL
);

CREATE TABLE mpa_ratings (
    id INT PRIMARY KEY,
    name VARCHAR(10) NOT NULL
);

CREATE TABLE genres (
    id INT PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

CREATE TABLE films (
    id INT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    release_date DATE,
    duration INT,
    rating_mpa_id INT NOT NULL,
    FOREIGN KEY (rating_mpa_id) REFERENCES mpa_ratings(id)
);

CREATE TABLE friendships (
    user_id INT NOT NULL,
    friend_id INT NOT NULL,
    status VARCHAR(10) NOT NULL CHECK (status IN ('unconfirmed', 'confirmed')),
    PRIMARY KEY (user_id, friend_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (friend_id) REFERENCES users(id)
);

CREATE TABLE film_genres (
    film_id INT NOT NULL,
    genre_id INT NOT NULL,
    PRIMARY KEY (film_id, genre_id),
    FOREIGN KEY (film_id) REFERENCES films(id),
    FOREIGN KEY (genre_id) REFERENCES genres(id)
);

CREATE TABLE likes (
    user_id INT NOT NULL,
    film_id INT NOT NULL,
    PRIMARY KEY (user_id, film_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (film_id) REFERENCES films(id)
);
```

Рекомендуется сохранить этот скрипт в файле, например: `db/schema.sql`, и сослаться на него из README.

### Примеры SQL-запросов для основных операций

**Все фильмы с рейтингом MPA и жанрами**

```sql
SELECT f.id,
       f.name,
       f.description,
       f.release_date,
       f.duration,
       mr.name AS mpa_rating,
       STRING_AGG(g.name, ', ' ORDER BY g.name) AS genres
FROM films f
LEFT JOIN mpa_ratings mr ON f.rating_mpa_id = mr.id
LEFT JOIN film_genres fg ON f.id = fg.film_id
LEFT JOIN genres g ON fg.genre_id = g.id
GROUP BY f.id, f.name, f.description, f.release_date, f.duration, mr.name
ORDER BY f.id;
```

**Все пользователи**

```sql
SELECT *
FROM users
ORDER BY id;
```

**Топ N популярных фильмов по количеству лайков**

```sql
SELECT f.id,
       f.name,
       COUNT(l.user_id) AS likes_count
FROM films f
LEFT JOIN likes l ON f.id = l.film_id
GROUP BY f.id, f.name
ORDER BY likes_count DESC
LIMIT 10; -- здесь 10 = N
```

**Список друзей пользователя (подтверждённая дружба)**

```sql
SELECT u2.id,
       u2.email,
       u2.login,
       u2.name,
       u2.birthday
FROM friendships fr
JOIN users u2 ON fr.friend_id = u2.id
WHERE fr.user_id = :user_id
  AND fr.status = 'confirmed';
```

**Список общих друзей двух пользователей**

```sql
SELECT u.id,
       u.email,
       u.login,
       u.name,
       u.birthday
FROM friendships fr1
JOIN friendships fr2
  ON fr1.friend_id = fr2.friend_id
JOIN users u ON u.id = fr1.friend_id
WHERE fr1.user_id = :user_id_1
  AND fr2.user_id = :user_id_2
  AND fr1.status = 'confirmed'
  AND fr2.status = 'confirmed';
```

---


```
```
