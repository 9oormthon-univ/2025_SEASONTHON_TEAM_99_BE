package seasonton.youthPolicy.domain.post.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import seasonton.youthPolicy.domain.member.domain.entity.User;
import seasonton.youthPolicy.domain.post.domain.entity.PostLike;
import seasonton.youthPolicy.domain.post.domain.entity.Posts;

import java.util.Optional;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    Optional<PostLike> findByUserAndPost(User user, Posts post);

    Long countByPostId(Long postId);
}
