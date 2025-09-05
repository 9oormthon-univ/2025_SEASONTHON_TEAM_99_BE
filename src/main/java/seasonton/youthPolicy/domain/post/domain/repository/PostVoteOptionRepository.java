package seasonton.youthPolicy.domain.post.domain.repository;

import org.checkerframework.common.util.report.qual.ReportCreation;
import org.springframework.data.jpa.repository.JpaRepository;
import seasonton.youthPolicy.domain.post.domain.entity.PostVoteOption;

@ReportCreation
public interface PostVoteOptionRepository extends JpaRepository<PostVoteOption, Long> {
}
