# Filmorate (Sprint 10)

Бэкенд-сервис на **Spring Boot 3.3 / Java 17** для работы с фильмами и пользователями. Данные пока хранятся **в памяти** (Map).  
Ветка задания: `controllers-films-users`.

## Что сделано ✅
- **Модели**: `Film`, `User` (поля по ТЗ).
- **REST-контроллеры**: `FilmController`, `UserController`  
  - `POST/PUT/GET` для `/films` и `/users`.
- **Хранение**: in-memory через сервисы (`FilmService`, `UserService`) с авто-генерацией ID.
- **Валидация (база)**: проверки из ТЗ, глобальный обработчик ошибок `GlobalExceptionHandler`.
- **Бизнес-правило**: дата релиза фильма **не раньше 28.12.1895**.
- **Логирование**: операции добавления/обновления и причины ошибок (Lombok `@Slf4j`).
- **Тесты**: unit-тесты валидации через `MockMvc` (`mvn test` проходит).

## Дополнительное задание ⭐
- Подключён `spring-boot-starter-validation`.
- Применены аннотации Jakarta Validation:  
  - `Film`: `@NotBlank name`, `@Size(max=200) description`, `@NotNull @PastOrPresent releaseDate`, `@NotNull @Positive duration`.
  - `User`: `@NotBlank @Email email`, `@NotBlank @Pattern(^\\S+$) login`, `name` может быть пустым (подставляется `login`), `@PastOrPresent birthday`.
- Во всех `POST/PUT` используется `@Valid @RequestBody`.

## Запуск
```bash
# из корня проекта
mvn spring-boot:run
# или
mvn clean package && java -jar target/filmorate-1.0.0.jar
Сервис поднимется на http://localhost:8080.
Эндпоинты
POST   /users       — создать пользователя
PUT    /users       — обновить пользователя
GET    /users       — список пользователей

POST   /films       — создать фильм
PUT    /films       — обновить фильм
GET    /films       — список фильмов
Примеры запросов
POST /users
Content-Type: application/json
{
  "email": "user@example.com",
  "login": "space_cadet",
  "name": "",
  "birthday": "1990-01-01"
}

POST /films
Content-Type: application/json
{
  "name": "Interstellar",
  "description": "A science fiction film.",
  "releaseDate": "2014-11-07",
  "duration": 169
}

Валидация (кратко)

Film: name не пустой; description ≤ 200; releaseDate не в будущем и ≥ 1895-12-28; duration > 0.

User: email валидный; login не пустой и без пробелов; name может быть пустым — подставится login; birthday не в будущем.
Ошибки → 400 с описанием по полям, отсутствующие ресурсы → 404.

Тесты

mvn test


Покрывают позитивные и граничные кейсы валидации для /films и /users.

