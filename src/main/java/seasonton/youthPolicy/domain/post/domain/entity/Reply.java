package seasonton.youthPolicy.domain.post.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import seasonton.youthPolicy.domain.model.entity.BaseEntity;
import seasonton.youthPolicy.domain.user.domain.entity.User;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reply extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content = "no contents";

    private boolean is_anonymous;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Posts post;
}
