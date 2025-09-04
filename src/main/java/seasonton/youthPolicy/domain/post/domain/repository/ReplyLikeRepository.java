package seasonton.youthPolicy.domain.post.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import seasonton.youthPolicy.domain.post.domain.entity.ReplyLike;

@Repository
public interface ReplyLikeRepository extends JpaRepository<ReplyLike, Long> {
}
