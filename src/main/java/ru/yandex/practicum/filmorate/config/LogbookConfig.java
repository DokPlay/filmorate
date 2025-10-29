package ru.yandex.practicum.filmorate.config;

import java.util.function.Predicate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.logbook.BodyFilter;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.json.JsonBodyFilters;

/**
 * SPRINT 11 (Доп. задание):
 * - настраиваем Logbook так, чтобы поле "email" в теле запросов/ответов
 *   скрывалось в логах (GDPR-friendly).
 *
 * SPRINT 11: FIX — в JsonBodyFilters нет delete(...).
 * Используем replaceJsonStringProperty(...) c предикатом по имени JSON-свойства.
 */
@Configuration
public class LogbookConfig {

  @Bean
  public Logbook logbook() {
    // SPRINT 11: маскируем все JSON-свойства с именем "email" (без учёта регистра)
    Predicate<String> isEmailProp = name -> "email".equalsIgnoreCase(name);

    BodyFilter maskEmail = JsonBodyFilters.replaceJsonStringProperty(
        isEmailProp,
        "****" // что подставлять вместо значения
    );

    // Если фильтров несколько, можно объединить: BodyFilter.merge(f1, f2, ...)
    return Logbook.builder()
        .bodyFilter(maskEmail)
        .build();
  }
}
