package seasonton.youthPolicy.domain.post.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import seasonton.youthPolicy.domain.model.entity.Region;
import seasonton.youthPolicy.domain.post.domain.entity.Posts;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Posts, Long> {

    @Query("SELECT p FROM Posts p JOIN FETCH p.region r ORDER BY p.createdAt DESC")
    List<Posts> findAllWithRegionOrderByCreatedAtDesc();

    List<Posts> findByRegion(Region region);

}
