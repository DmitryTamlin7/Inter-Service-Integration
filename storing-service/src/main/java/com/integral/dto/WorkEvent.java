package com.integral.dto;

public record WorkEvent(
        Long workId,
        String studentName,
        String minioPath,
        String status) {

}
