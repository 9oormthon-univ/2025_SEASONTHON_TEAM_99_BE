package seasonton.youthPolicy.domain.user.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import seasonton.youthPolicy.domain.user.domain.entity.Report;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
}
