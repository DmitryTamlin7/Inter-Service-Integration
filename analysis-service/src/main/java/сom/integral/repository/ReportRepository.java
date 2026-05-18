package сom.integral.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import сom.integral.domain.Report;

import java.util.Optional;

public interface ReportRepository extends JpaRepository<Report, Long> {
    Optional<Report> findByWorkId(Long workId);
}
