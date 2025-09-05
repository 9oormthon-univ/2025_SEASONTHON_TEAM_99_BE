package seasonton.youthPolicy.domain.policy.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import seasonton.youthPolicy.domain.policy.domain.entity.PolicyReplyLike;

import java.util.Optional;

@Repository
public interface PolicyReplyLikeRepository extends JpaRepository<PolicyReplyLike, Long> {

    // 특정 유저가 특정 댓글에 좋아요를 눌렀는지 확인
    Optional<PolicyReplyLike> findByUserIdAndPolicyReplyId(Long userId, Long replyId);

    // 특정 댓글의 좋아요 개수
    Long countByPolicyReplyId(Long replyId);

    // 좋아요 삭제용
    void deleteByUserIdAndPolicyReplyId(Long userId, Long replyId);
}
