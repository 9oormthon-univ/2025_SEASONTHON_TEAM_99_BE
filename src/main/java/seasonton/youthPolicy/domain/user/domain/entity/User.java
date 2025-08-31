package seasonton.youthPolicy.domain.user.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import seasonton.youthPolicy.domain.model.entity.BaseEntity;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 25)
    private String socialId;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(length = 50)
    private String nickname;

    private String profileImageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

}
