package сom.integral.consumer;


import io.minio.*;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import сom.integral.client.WordCloudClient;
import сom.integral.domain.Report;
import сom.integral.domain.ValidationReport;
import сom.integral.dto.WorkEventDto;
import сom.integral.repository.ReportRepository;
import сom.integral.service.ValidationService;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
@RequiredArgsConstructor
public class WorkEventConsumer {

    private final MinioClient minioClient;
    private final ValidationService validationService;
    private final ReportRepository reportRepository;
    private final WordCloudClient wordCloudClient;

    @Value("${minio.bucketName}")
    private String bucketName;

    @RabbitListener(queues = "${app.rabbit.queue}")
    public void processWorkEvent(WorkEventDto event){
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
            String wordCloudMinioPath = null;

            if (report.isAccepted()){
                log.info("Валидация Успешна! Статус: {}", report.getStatus());

                try (InputStream fileStream = minioClient.getObject(
                        GetObjectArgs.builder()
                                .bucket(bucketName)
                                .object(event.getMinioPath())
                                .build()
                )){
                    log.info("Файл {} скачан в поток для анализа текста.", event.getMinioPath());

                    String fileContent = new String(fileStream.readAllBytes(), StandardCharsets.UTF_8);
                    byte[] imageBytes = wordCloudClient.generateWordCloud(fileContent);

                    if (imageBytes != null && imageBytes.length > 0){
                        wordCloudMinioPath = "plots/cloud-" + java.util.UUID.randomUUID() + ".png";

                        try (ByteArrayInputStream baits = new ByteArrayInputStream(imageBytes)){
                            minioClient.putObject(
                                    PutObjectArgs.builder()
                                            .bucket(bucketName)
                                            .object(wordCloudMinioPath)
                                            .stream(baits, imageBytes.length, -1)
                                            .contentType("image/png")
                                            .build()
                            );
                            log.info("Картинка Облака слов успешно сохранена в S3 хранилище с именем: {}", wordCloudMinioPath);
                        }
                    }
                }
                catch (Exception e){
                    log.error("Ошибка во времени генерации или сохранения в хранилище: {}", e.getMessage());
                }
            }
            else {
                log.warn("Валидация НЕ пройдена! Статус: {}", report.getStatus());
                log.warn("Список замечаний: {}", report.getRemarks());
            }
            saveReport(event.getWorkId(), report, wordCloudMinioPath);
        } catch (Exception e) {
            log.error("Критическая ошибка при обработке сообщения с ID: {} Ошибка: {}", event.getWorkId(), e.getMessage());
        }
    }

    private void  saveReport(Long workId, ValidationReport validationReport, String wordCloudPath){
        log.info("Сохранение финального отчета в для работы {} В БД", workId);

        try {
            Report report = new Report();
            report.setWorkId(workId);
            report.setStatus(validationReport.getStatus());
            report.setRemarks(validationReport.getRemarks());
            report.setWordCloudPath(wordCloudPath);

            reportRepository.save(report);
            log.info("Отчет для работы ID: {} Записан в БД", workId);
        }
        catch (Exception e){
            log.error("Не удалось сохранить отчет в БД дя работы с ID: {} : {}", workId, e.getMessage());
        }
    }
}
