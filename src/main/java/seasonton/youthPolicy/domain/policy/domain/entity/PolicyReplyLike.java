package seasonton.youthPolicy.domain.policy.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import seasonton.youthPolicy.domain.member.domain.entity.User;
import seasonton.youthPolicy.domain.model.entity.BaseEntity;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PolicyReplyLike extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_reply_id", nullable = false)
    private PolicyReply policyReply;
}
