package seasonton.youthPolicy.domain.policy.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import seasonton.youthPolicy.domain.policy.domain.entity.Policy;

@Repository
public interface PolicyRepository extends JpaRepository<Policy, Long> {
}
