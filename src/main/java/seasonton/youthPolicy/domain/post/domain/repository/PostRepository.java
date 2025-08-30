package seasonton.youthPolicy.domain.post.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import seasonton.youthPolicy.domain.post.domain.entity.Posts;

@Repository
public interface PostRepository extends JpaRepository<Posts, Long> {
}
