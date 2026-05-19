package com.integral.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import сom.integral.domain.ValidationReport;
import сom.integral.service.ValidationService;

import static org.junit.jupiter.api.Assertions.*;

class ValidationServiceTest {

    private ValidationService validationService;

    @BeforeEach
    void setUp() {
        validationService = new ValidationService();
    }

    @Test
    @DisplayName("Успешная валидация: правильное расширение и допустимый размер")
    void validate_Success_ShouldReturnAccepted() {

        String validPath = "works/user123/diploma.txt";
        long validSize = 5000L;

        ValidationReport report = validationService.validate(validPath, validSize);

        assertNotNull(report);
        assertTrue(report.isAccepted());
        assertEquals("ПРИНЯТО", report.getStatus());
        assertTrue(report.getRemarks().isEmpty());
    }

    @Test
    @DisplayName("Ошибка валидации: запрещенное расширение файла")
    void validate_WrongExtension_ShouldReturnRejected() {
        String invalidPath = "works/user123/cheating_photo.png";
        long validSize = 5000L;

        ValidationReport report = validationService.validate(invalidPath, validSize);


        assertNotNull(report);
        assertFalse(report.isAccepted());
        assertEquals("ТРЕБУЕТСЯ ДОРАБОТКА", report.getStatus());
        assertFalse(report.getRemarks().isEmpty());
        assertTrue(report.getRemarks().stream().anyMatch(r -> r.contains("расширение") || r.contains("формат") || r.contains("тип")));
    }

    @Test
    @DisplayName("Ошибка валидации: файл слишком большой")
    void validate_FileTooLarge_ShouldReturnRejected() {
        String validPath = "works/user123/huge_file.txt";
        long tooLargeSize = 50 * 1024 * 1024L;


        ValidationReport report = validationService.validate(validPath, tooLargeSize);


        assertNotNull(report);
        assertFalse(report.isAccepted());
        assertEquals("ТРЕБУЕТСЯ ДОРАБОТКА", report.getStatus());
        assertFalse(report.getRemarks().isEmpty());
        assertTrue(report.getRemarks().stream().anyMatch(r -> r.contains("размер") || r.contains("большой") || r.contains("превыш")));
    }

    @Test
    @DisplayName("Ошибка валидации: текст равен null")
    void validate_NullText_ShouldReturnRejectedOrHandleGracefully() {
        Long workId = 103L;

        ValidationReport report = validationService.validate(null, workId);

        assertNotNull(report);
        assertFalse(report.isAccepted());
        assertEquals("ТРЕБУЕТСЯ ДОРАБОТКА", report.getStatus());
        assertFalse(report.getRemarks().isEmpty());
    }
}
