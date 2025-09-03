package seasonton.youthPolicy.domain.policy.domain.repository;

import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.jpa.repository.JpaRepository;
import seasonton.youthPolicy.domain.member.domain.entity.User;
import seasonton.youthPolicy.domain.policy.domain.entity.PolicyLike;

import java.util.Optional;

@ReadingConverter
public interface PolicyLikeRepository extends JpaRepository<PolicyLike, Long> {

    // 특정 유저가 특정 정책(plcyNo)에 좋아요를 눌렀는지 확인
    Optional<PolicyLike> findByUserAndPlcyNo(User user, String plcyNo);

    // 특정 정책(plcyNo)의 좋아요 개수 카운트
    long countByPlcyNo(String plcyNo);
}
