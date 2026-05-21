package com.integral.dto;
/**
 * Обменная модель данных (DTO) для получения метаданных загружаемой работы от клиента.
 * <p>
 * Содержит правила валидации для предотвращения сохранения
 * некорректных или неполных данных о студенте.
 *
 * @author [Dmitry]
 */
public record WorkEvent(
        Long workId,
        String studentName,
        String minioPath,
        String status) {

}
