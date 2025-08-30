package seasonton.youthPolicy.domain.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class PostResponseDTO {

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PostCreateResponse {

        @NotBlank(message = "제목은 필수 선택 값입니다.")
        @Size(max = 50)
        private String title;

        @Size(max = 2000)
        private String content;

        private boolean is_anonymous;

        private String regionName;
    }
}
