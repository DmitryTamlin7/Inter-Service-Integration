package сom.integral.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import сom.integral.domain.Report;
import сom.integral.repository.ReportRepository;

/**
 * REST-контроллер для предоставления результатов анализа студенческих работ.
 * <p>
 * Предоставляет клиенту доступ к статистике, сгенерированным облакам слов и
 * результатам автоматических проверок, выполненных микросервисом {@code analysis-service}.
 * Есть 2 пути получения:
 *  запрос на получения всех отчетов списком JSON
 *  получения конкретного отчета по workId
 * @author [Dmitry]
 *
 */
@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@Slf4j
public class ReportController {

    private final ReportRepository repository;

    @CrossOrigin("*")
    @GetMapping("/{work_id}")
    public ResponseEntity<Report> getReportByWorkId(@PathVariable("work_id") Long workId){
        log.info("Получен запрос на получение отчета для работы {}", workId);

        return repository.findByWorkId(workId)
                .map(report -> {
                    log.info("Отчет найден в БД");
                    return ResponseEntity.ok(report);
                })
                .orElseGet(() -> {
                    log.warn("Отчет по работе {} найден", workId);
                    return ResponseEntity.notFound().build();
                });
    }

    @CrossOrigin("*")
    @GetMapping
    public ResponseEntity<java.util.List<Report>> getAllReports() {
        log.info("Получен запрос на получение всех отчетов");
        return ResponseEntity.ok(repository.findAll());
    }
}
