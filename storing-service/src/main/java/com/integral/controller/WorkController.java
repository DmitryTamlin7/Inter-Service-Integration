package com.integral.controller;


import lombok.RequiredArgsConstructor;
import com.integral.model.Work;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.integral.service.WorkService;

/**
 * REST-контроллер для управления загрузкой и хранением студенческих лабораторных работ.
 * <p>
 * Является входной точкой (API) сервиса хранения (storing-service).
 * Принимает файлы от клиентов, делегирует их физическое сохранение в MinIO и
 * инициирует асинхронный процесс анализа через брокер сообщений RabbitMQ.
 *
 * @author [Dmitry]
 * @version 1.0
 *
 */
@RestController
@RequestMapping("/works")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class WorkController {

    private final WorkService service;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Work> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("studentName") String studentName){

        Work savedWork = service.uploadWork(file, studentName);
        return ResponseEntity.ok(savedWork);
    }


}
