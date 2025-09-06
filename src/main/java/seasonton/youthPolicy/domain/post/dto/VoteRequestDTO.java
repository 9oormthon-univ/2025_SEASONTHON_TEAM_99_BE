package seasonton.youthPolicy.domain.post.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class VoteRequestDTO {

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class VoteRequest {
        private Long voteId;
        private List<Long> optionIds; // 복수 선택 가능 대비
    }
}
