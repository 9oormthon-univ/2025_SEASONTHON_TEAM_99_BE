package seasonton.youthPolicy.domain.post.domain.repository;

import org.checkerframework.common.util.report.qual.ReportCreation;
import org.springframework.data.jpa.repository.JpaRepository;
import seasonton.youthPolicy.domain.post.domain.entity.PostVote;
import seasonton.youthPolicy.domain.post.domain.entity.Posts;

import java.util.Optional;

@ReportCreation
public interface PostVoteRepository extends JpaRepository<PostVote, Long> {

    Optional<PostVote> findByPost(Posts post);
}
