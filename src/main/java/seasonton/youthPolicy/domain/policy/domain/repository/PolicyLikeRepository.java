package seasonton.youthPolicy.domain.policy.domain.repository;

import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.jpa.repository.JpaRepository;
import seasonton.youthPolicy.domain.policy.domain.entity.PolicyLike;

@ReadingConverter
public interface PolicyLikeRepository extends JpaRepository<PolicyLike, Long> {
}
