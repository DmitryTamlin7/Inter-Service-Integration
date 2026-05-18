package сom.integral.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import сom.integral.domain.Report;
import сom.integral.repository.ReportRepository;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@Slf4j
public class ReportController {

    private final ReportRepository repository;

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
}
