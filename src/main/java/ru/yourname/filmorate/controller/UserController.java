package ru.yourname.filmorate.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yourname.filmorate.model.User;
import ru.yourname.filmorate.service.UserService;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @PostMapping
  public User create(@Valid @RequestBody User user) {
    log.debug("POST /users payload: {}", user);
    return userService.add(user);
  }

  @PutMapping
  public User update(@Valid @RequestBody User user) {
    log.debug("PUT /users payload: {}", user);
    return userService.update(user);
  }

  @GetMapping
  public List<User> getAll() {
    return userService.getAll();
  }
}
