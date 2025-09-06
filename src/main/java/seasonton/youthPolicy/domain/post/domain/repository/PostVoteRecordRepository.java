package seasonton.youthPolicy.domain.post.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import seasonton.youthPolicy.domain.member.domain.entity.User;
import seasonton.youthPolicy.domain.post.domain.entity.PostVote;
import seasonton.youthPolicy.domain.post.domain.entity.PostVoteRecord;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostVoteRecordRepository extends JpaRepository<PostVoteRecord, Long> {
    Optional<PostVoteRecord> findByVoteAndUser(PostVote vote, User user);
    List<PostVoteRecord> findAllByVoteAndUser(PostVote vote, User user);
}
