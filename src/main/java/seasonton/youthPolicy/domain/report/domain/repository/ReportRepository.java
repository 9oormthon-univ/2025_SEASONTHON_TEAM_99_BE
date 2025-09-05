package seasonton.youthPolicy.domain.report.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import seasonton.youthPolicy.domain.model.entity.Region;
import seasonton.youthPolicy.domain.report.domain.entity.Report;

import java.time.YearMonth;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    Report findByYearmonthAndRegion(YearMonth yearMonth, Region region);
}
