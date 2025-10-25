package ru.yandex.practicum.filmorate.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = ru.yandex.practicum.filmorate.FilmorateApplication.class)
@AutoConfigureMockMvc
class FilmControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldFailWhenNameBlank() throws Exception {
        Map<String, Object> payload = Map.of(
                "name", " ",
                "description", "desc",
                "releaseDate", LocalDate.now().toString(),
                "duration", 120
        );

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailWhenDescriptionTooLong() throws Exception {
        String longDesc = "x".repeat(201);
        Map<String, Object> payload = Map.of(
                "name", "Ok",
                "description", longDesc,
                "releaseDate", LocalDate.now().toString(),
                "duration", 120
        );

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailWhenReleaseBefore1895_12_28() throws Exception {
        Map<String, Object> payload = Map.of(
                "name", "Ok",
                "description", "Ok",
                "releaseDate", "1895-12-27",
                "duration", 120
        );

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailWhenDurationNotPositive() throws Exception {
        Map<String, Object> payload = Map.of(
                "name", "Ok",
                "description", "Ok",
                "releaseDate", "2000-01-01",
                "duration", 0
        );

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldCreateWhenAllValid() throws Exception {
        Map<String, Object> payload = Map.of(
                "name", "Valid Film",
                "description", "Ok",
                "releaseDate", "2000-01-01",
                "duration", 100
        );

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk());
    }
}
