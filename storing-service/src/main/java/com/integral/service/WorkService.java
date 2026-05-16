package com.integral.service;

import com.integral.dto.WorkEvent;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import com.integral.model.Work;
import com.integral.model.WorkStatus;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.integral.repository.WorkRepository;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class WorkService {
    private final MinioClient minioClient;
    private final WorkRepository repository;
    private final RabbitTemplate template;

    @Value("${minio.bucket}")
    private String bucketName;

    @Value("${app.rabbit.exchange}")
    private String exchangeName;

    @Value("${app.rabbit.routing-key}")
    private String routingKey;

    public Work uploadWork(MultipartFile file, String studentName){
        try {
            String fileExtension = getExtension(file.getOriginalFilename());
            String uniqueMinioName = UUID.randomUUID().toString() + fileExtension;

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(uniqueMinioName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
            log.info("Файл  {} успешно загружен в S3 хранилище! ", uniqueMinioName);
            Work work = Work.builder()
                    .studentName(studentName)
                    .originalFileName(file.getOriginalFilename())
                    .minioPath(uniqueMinioName)
                    .status(WorkStatus.UPLOADED)
                    .uploadedAt(LocalDateTime.now())
                    .build();

            Work saved = repository.save(work);
            log.info("Запись о отправке решения сохранена");

            WorkEvent event = new WorkEvent(
                    saved.getId(),
                    saved.getStudentName(),
                    saved.getMinioPath(),
                    saved.getStatus().name()
            );

            template.convertAndSend(exchangeName, routingKey, event);
            log.info("Событие WorkEvent отправлен в брокер сообщений для ID: {}", saved.getId());

            return saved;
        }
        catch (Exception e){
            log.error("Ошибка при загрузке файла ", e);
            throw new RuntimeException("Не удалось отправить решение и сохранить файл: " + e.getMessage());
        }
    }

    private String getExtension(String fileName){
        if (fileName != null && fileName.contains(".")){
            return fileName.substring(fileName.lastIndexOf("."));
        }
        return "";
    }



}
