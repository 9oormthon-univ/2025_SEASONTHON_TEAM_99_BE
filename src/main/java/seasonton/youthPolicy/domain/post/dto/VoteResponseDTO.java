package seasonton.youthPolicy.domain.post.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class VoteResponseDTO {

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PostVoteResponse {
        private Long voteId;
        private String question;
        private boolean multipleChoice;
        private LocalDateTime endDate;
        private List<OptionResponse> options;

        @Getter
        @Builder
        @AllArgsConstructor
        @NoArgsConstructor
        public static class OptionResponse {
            private Long optionId;
            private String optionText;
            private int voteCount;
        }
    }

    // 투표하기
    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class VoteResultResponse {
        private Long voteId;
        private String question;
        private boolean multipleChoice;
        private LocalDateTime endDate;
        private List<OptionResult> options;

        @Getter
        @Builder
        @AllArgsConstructor
        @NoArgsConstructor
        public static class OptionResult {
            private Long optionId;
            private String optionText;
            private int voteCount;
            private boolean voted;   // 현재 사용자가 선택했는지 여부
        }
    }
}
