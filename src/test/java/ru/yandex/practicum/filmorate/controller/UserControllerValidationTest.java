package ru.yandex.practicum.filmorate.controller;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = ru.yandex.practicum.filmorate.FilmorateApplication.class)
@AutoConfigureMockMvc
class UserControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldFailWhenEmailInvalid() throws Exception {
        Map<String, Object> payload = Map.of(
                "email", "not-an-email",
                "login", "user",
                "name", "Name",
                "birthday", "1990-01-01"
        );

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailWhenLoginHasSpaces() throws Exception {
        Map<String, Object> payload = Map.of(
                "email", "user@example.com",
                "login", "bad login",
                "name", "Name",
                "birthday", "1990-01-01"
        );

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldDefaultNameToLoginWhenBlank() throws Exception {
        // Делаем логин/email уникальными, чтобы не ловить UNIQUE-ошибку из БД
        String login = "space_cadet_" + System.nanoTime();
        String email = login + "@example.com";

        Map<String, Object> payload = Map.of(
                "email", email,
                "login", login,
                "name", "",
                "birthday", "1990-01-01"
        );

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(login)));
    }
}
