Table friends {
  // Если A добавил B -> одна строка (A,B)
  // Если B добавил A -> вторая строка (B,A)
  // Наличие обеих строк = подтвержденная дружба

  user_id   int  [not null] // кто добавил
  friend_id int  [not null] // кого добавил
  created_at datetime

  indexes {
    (user_id, friend_id) [pk] // одна запись на направление
  }

  Note: 'Дружба подтверждена, когда существуют обе записи: (A,B) и (B,A).'
}
