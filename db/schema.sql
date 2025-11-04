-- Dictionary: MPA ratings
CREATE TABLE mpa_ratings (
    id          SMALLINT      PRIMARY KEY,
    code        VARCHAR(10)   NOT NULL UNIQUE,  -- G, PG, PG-13, R, NC-17
    name        VARCHAR(100)  NOT NULL          -- full description
);

INSERT INTO mpa_ratings (id, code, name) VALUES
(1, 'G',     'General audiences'),
(2, 'PG',    'Parental guidance suggested'),
(3, 'PG-13', 'Parents strongly cautioned'),
(4, 'R',     'Restricted'),
(5, 'NC-17', 'No one 17 and under admitted');

-- Dictionary: genres
CREATE TABLE genres (
    genre_id    SMALLINT      PRIMARY KEY,
    name        VARCHAR(100)  NOT NULL UNIQUE
);

-- Core: users
CREATE TABLE users (
    user_id   BIGSERIAL      PRIMARY KEY,
    email     VARCHAR(255)   NOT NULL UNIQUE,
    login     VARCHAR(64)    NOT NULL UNIQUE,
    name      VARCHAR(255)   NOT NULL,
    birthday  DATE           NOT NULL,
    CHECK (birthday <= CURRENT_DATE)
);

-- Core: films
CREATE TABLE films (
    film_id        BIGSERIAL      PRIMARY KEY,
    name           VARCHAR(255)   NOT NULL,
    description    VARCHAR(200),
    release_date   DATE           NOT NULL,
    duration       INTEGER        NOT NULL,
    mpa_rating_id  SMALLINT       NOT NULL REFERENCES mpa_ratings(id),
    CHECK (duration > 0),
    CHECK (release_date >= DATE '1895-12-28')
);

-- Many-to-many: film <-> genre
CREATE TABLE film_genres (
    film_id   BIGINT    NOT NULL REFERENCES films(film_id) ON DELETE CASCADE,
    genre_id  SMALLINT  NOT NULL REFERENCES genres(genre_id),
    PRIMARY KEY (film_id, genre_id)
);

-- Likes: user <-> film
CREATE TABLE film_likes (
    film_id    BIGINT   NOT NULL REFERENCES films(film_id) ON DELETE CASCADE,
    user_id    BIGINT   NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY (film_id, user_id)
);

-- Friendship statuses dictionary
CREATE TABLE friendship_statuses (
    id          SMALLINT      PRIMARY KEY,
    code        VARCHAR(20)   NOT NULL UNIQUE,  -- PENDING / CONFIRMED
    description VARCHAR(255)
);

INSERT INTO friendship_statuses (id, code, description) VALUES
(1, 'PENDING',   'Unconfirmed friend request'),
(2, 'CONFIRMED', 'Confirmed friendship');

-- Friendships between users
CREATE TABLE friendships (
    user_id    BIGINT     NOT NULL REFERENCES users(user_id)  ON DELETE CASCADE,
    friend_id  BIGINT     NOT NULL REFERENCES users(user_id)  ON DELETE CASCADE,
    status_id  SMALLINT   NOT NULL REFERENCES friendship_statuses(id),
    created_at TIMESTAMP  NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, friend_id),
    CHECK (user_id <> friend_id)
);
