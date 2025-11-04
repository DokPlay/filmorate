Enum FriendStatus {
  unconfirmed
  confirmed
}

Table users {
  id        int          [pk]
  email     varchar(255) [not null, unique]
  login     varchar(255) [not null, unique]
  name      varchar(255) [not null]
  birthday  date         [not null]
}

Table mpa_ratings {
  id    int       [pk]
  name  varchar(10) [not null]
}

Table genres {
  id    int          [pk]
  name  varchar(100) [not null]
}

Table films {
  id             int          [pk]
  name           varchar(255) [not null]
  description    text
  release_date   date
  duration       int
  rating_mpa_id  int          [not null] // Ref defined below
}

Table friendships {
  user_id   int          [not null]
  friend_id int          [not null]
  status    FriendStatus [not null]

  indexes {
    (user_id, friend_id) [pk]
  }
}

Table film_genres {
  film_id  int [not null]
  genre_id int [not null]

  indexes {
    (film_id, genre_id) [pk]
  }
}

Table likes {
  user_id int [not null]
  film_id int [not null]

  indexes {
    (user_id, film_id) [pk]
  }
}

// Relationships
Ref: films.rating_mpa_id > mpa_ratings.id

Ref: friendships.user_id   > users.id
Ref: friendships.friend_id > users.id

Ref: film_genres.film_id  > films.id
Ref: film_genres.genre_id > genres.id

Ref: likes.user_id > users.id
Ref: likes.film_id > films.id
