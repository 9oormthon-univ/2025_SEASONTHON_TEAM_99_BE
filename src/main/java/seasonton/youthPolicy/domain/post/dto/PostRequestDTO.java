package seasonton.youthPolicy.domain.post.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class PostRequestDTO {

    // 댓글 작성
    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReplyCreateRequest {

        private String content;
        private boolean isAnonymous;
    }

    // 댓글 수정
    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReplyUpdateRequest {

        private String content;

        @JsonProperty("anonymous")
        private boolean isAnonymous;
    }

}
