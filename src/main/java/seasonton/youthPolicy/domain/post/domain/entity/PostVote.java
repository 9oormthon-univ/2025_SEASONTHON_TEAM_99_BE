package seasonton.youthPolicy.domain.post.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import seasonton.youthPolicy.domain.model.entity.BaseEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostVote extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String question;              // 투표 질문
    private LocalDateTime endDate;        // 투표 종료일 (선택적)
    private boolean multipleChoice;       // 복수 선택 가능 여부

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Posts post;

    @OneToMany(mappedBy = "vote", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PostVoteOption> options = new ArrayList<>();

    public void updateVote(String question, LocalDateTime endDate, boolean multipleChoice, List<PostVoteOption> newOptions) {
        this.question = question;
        this.endDate = endDate;
        this.multipleChoice = multipleChoice;
        this.options.clear();
        this.options.addAll(newOptions);
    }

}
