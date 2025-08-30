package seasonton.youthPolicy.domain.test.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import seasonton.youthPolicy.domain.test.domain.entity.TestEntity;

@Repository
public interface TestRepository extends JpaRepository<TestEntity, Long> {
}
