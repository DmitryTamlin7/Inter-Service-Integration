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
import сom.integral.domain.Report;
import сom.integral.domain.ValidationReport;
import сom.integral.dto.WorkEventDto;
import сom.integral.repository.ReportRepository;
import сom.integral.service.ValidationService;
import java.io.InputStream;

@Component
@Slf4j
@RequiredArgsConstructor
public class WorkEventConsumer {

    private final MinioClient minioClient;
    private final ValidationService validationService;
    private final ReportRepository reportRepository;

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

    private void  saveReport(Long workId, ValidationReport validationReport){

        log.info("Сохранение финального отчета в для работы {} В БД", workId);
        try {
            Report report = new Report();
            report.setWorkId(workId);
            report.setStatus(validationReport.getStatus());
            report.setRemarks(validationReport.getRemarks());
            report.setWordCloudPath(null);

            reportRepository.save(report);
            log.info("Отчет для работы ID: {} Записан в БД", workId);
        }
        catch (Exception e){
            log.error("Не удалось сохранить отчет в БД дя работы с ID: {} : {}", workId, e.getMessage());
        }
    }
}
