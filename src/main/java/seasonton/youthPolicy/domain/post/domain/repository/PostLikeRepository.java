package seasonton.youthPolicy.domain.post.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import seasonton.youthPolicy.domain.post.domain.entity.PostLike;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
}
