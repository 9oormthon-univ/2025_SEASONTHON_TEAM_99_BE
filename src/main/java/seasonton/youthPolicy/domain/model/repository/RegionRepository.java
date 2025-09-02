package seasonton.youthPolicy.domain.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import seasonton.youthPolicy.domain.model.entity.Region;

@Repository
public interface RegionRepository extends JpaRepository<Region, Long> {
}
