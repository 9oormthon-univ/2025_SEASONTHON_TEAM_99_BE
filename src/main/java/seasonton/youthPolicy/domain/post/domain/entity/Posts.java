package seasonton.youthPolicy.domain.post.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import seasonton.youthPolicy.domain.model.entity.BaseEntity;
import seasonton.youthPolicy.domain.model.entity.Region;
import seasonton.youthPolicy.domain.member.domain.entity.User;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Posts extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, nullable = false)
    private String title;

    @Column(length = 2000)
    private String content;

    @Column(length = 50)
    private String writer;

    @Column(nullable = false)
    private boolean isAnonymous;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 글 수정 관련
    public void updatePost(String title, String content, boolean isAnonymous, String writer, Region region) {
        this.title = title;
        this.content = content;
        this.isAnonymous = isAnonymous;
        this.writer = writer;
        this.region = region;
    }
}
