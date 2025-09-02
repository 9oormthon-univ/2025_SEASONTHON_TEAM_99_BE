package seasonton.youthPolicy.domain.policy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class PolicyRequestDTO {

    // 댓글 생성
    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Create {
        private String content;
        private boolean isAnonymous;
        private String plcyNo;   // 정책 번호
        private String plcyNm;   // 정책 이름
    }

    // 댓글 수정
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReplyUpdateRequest {
        private String content;
        private boolean isAnonymous;
    }

}
