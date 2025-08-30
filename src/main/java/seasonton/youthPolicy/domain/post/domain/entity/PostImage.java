package seasonton.youthPolicy.domain.post.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import seasonton.youthPolicy.domain.model.entity.BaseEntity;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostImage extends BaseEntity {

    @Id @GeneratedValue
    private Long id;

    // 원본 파일명
    private String originalName;

    // 버켓에 저장되는 파일명
    private String storedName;

    // 접근 url
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Posts post;

}
