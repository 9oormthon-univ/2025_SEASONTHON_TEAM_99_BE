package seasonton.youthPolicy.domain.post.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import seasonton.youthPolicy.domain.post.domain.entity.Posts;
import seasonton.youthPolicy.domain.post.domain.entity.Reply;

import java.util.List;

@Repository
public interface ReplyRepository extends JpaRepository<Reply, Long> {

    @Query("SELECT r FROM Reply r JOIN FETCH r.user WHERE r.post.id = :postId ORDER BY r.createdAt ASC")
    List<Reply> findAllByPostIdWithUser(@Param("postId") Long postId);

    List<Reply> findAllByPost(Posts post);
}
