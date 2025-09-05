package seasonton.youthPolicy.domain.post.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import seasonton.youthPolicy.domain.member.domain.entity.User;
import seasonton.youthPolicy.domain.post.domain.entity.Reply;
import seasonton.youthPolicy.domain.post.domain.entity.ReplyLike;

import java.util.Optional;

@Repository
public interface ReplyLikeRepository extends JpaRepository<ReplyLike, Long> {

     Optional<ReplyLike> findByUserAndReply(User user, Reply reply);

     Long countByReplyId(Long replyId);
}
