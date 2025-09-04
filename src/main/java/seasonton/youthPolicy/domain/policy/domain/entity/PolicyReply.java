package seasonton.youthPolicy.domain.policy.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import seasonton.youthPolicy.domain.model.entity.BaseEntity;
import seasonton.youthPolicy.domain.member.domain.entity.User;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PolicyReply extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content = "no content";

    private boolean isAnonymous;

    private String plcyNo;   // 정책 번호

    private String plcyNm;   // 정책 이름

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public void updateReply(String content, boolean isAnonymous) {
        this.content = content;
        this.isAnonymous = isAnonymous;
    }
}