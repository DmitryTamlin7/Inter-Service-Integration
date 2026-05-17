package сom.integral.consumer;


import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import сom.integral.domain.ValidationReport;
import сom.integral.dto.WorkEventDto;
import сom.integral.service.ValidationService;
import java.io.InputStream;

@Component
@Slf4j
@RequiredArgsConstructor
public class WorkEventConsumer {

    private final MinioClient minioClient;
    private final ValidationService validationService;

    @Value("${minio.bucketName}")
    private String bucketName;

    @RabbitListener(queues = "${app.rabbit.queue}")
    private void processWorkEvent(WorkEventDto event){
        log.info("Событие получено. ID для работы: {}, Студент: {}", event.getWorkId(), event.getStudentName());
        try {
            StatObjectResponse stat = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(event.getMinioPath())
                            .build()
            );
            long fileSize = stat.size();

            ValidationReport  report = validationService.validate(event.getMinioPath(), fileSize);

            if (report.isAccepted()){
                log.info("Валидация Успешна! Статус: {}", report.getStatus());

                try (InputStream fileStream = minioClient.getObject(
                        GetObjectArgs.builder()
                                .bucket(bucketName)
                                .object(event.getMinioPath())
                                .build()
                )){
                    log.info("Файл {} Скачен в потом InputStream Для работы", event.getMinioPath());
                   // TODO: Доделать
                }
            }
            else {
                log.warn("Валидация НЕ пройдена! Статус: {}", report.getStatus());
                log.warn("Список замечаний: {}", report.getRemarks());
            }
            saveReport(event.getWorkId(), report);
        } catch (Exception e) {
            log.error("Критическая ошибка при обработке сообщения с ID: {} Ошибка: {}", event.getWorkId(), e.getMessage());
        }
    }

    private void  saveReport(Long workId, ValidationReport report){
        log.info("отчет готов к сохранению");
    }
}
