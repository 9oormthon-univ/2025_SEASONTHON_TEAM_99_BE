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
    public static class PostVoteCreateRequest {
        private String question;
        private List<String> options;
        private LocalDateTime endDate;
        private boolean multipleChoice;
    }

}
