package seasonton.youthPolicy.domain.post.domain.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import seasonton.youthPolicy.domain.post.domain.entity.Posts;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Posts, Long> {

    @Query("SELECT p FROM Posts p JOIN FETCH p.region r ORDER BY p.createdAt DESC")
    List<Posts> findAllWithRegionOrderByCreatedAtDesc();

    @Query("""
    select p as post, count(pl.id) as likeCount
    from Posts p
    left join PostLike pl on pl.post = p
    where function('year', p.createdAt) = :year
      and function('month', p.createdAt) = :month
      and p.region.id = :regionId
    group by p
    order by likeCount desc, p.id desc
    """)
    List<Posts> findTopByYearMonthAndRegionOrderByLikeDesc(
            @Param("year") int year,
            @Param("month") int month,
            @Param("regionId") Long regionId,
            Pageable pageable
    );

}
