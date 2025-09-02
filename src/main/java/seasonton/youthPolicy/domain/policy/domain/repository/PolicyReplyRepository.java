package seasonton.youthPolicy.domain.policy.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import seasonton.youthPolicy.domain.policy.domain.entity.PolicyReply;

import java.util.List;

@Repository
public interface PolicyReplyRepository extends JpaRepository<PolicyReply, Long> {

    List<PolicyReply> findByPlcyNo(String plcyNo);

}
