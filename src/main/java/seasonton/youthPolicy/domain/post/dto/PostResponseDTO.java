package seasonton.youthPolicy.domain.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import seasonton.youthPolicy.domain.post.domain.entity.PostImage;

import java.time.LocalDateTime;
import java.util.List;

public class PostResponseDTO {

    // 글 생성
    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PostCreateResponse {

        private String title;
        private String content;
        private boolean isAnonymous;

        @Size(max = 50)
        private String regionName;
    }

    // 글 목록 조회
    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PostListResponse {

        private Long postId;
        private String title;
        private String regionName;
        private LocalDateTime createdAt;
        private Long likeCount;
    }

    // 게시글 좋아요 순 조회
    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PostLikeListResponse {
        private Long id;
        private String title;
        private String content;
        private Long likeCount;
        private String regionName;
        private LocalDateTime createdAt;
    }


    // 지역별 게시글 목록 조회
    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PostRegionListResponse {
        private Long postId;
        private String title;
        private String regionName;
        private LocalDateTime createdAt;
    }

    // 글 상세 조회
    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PostDetailResponse {

        private Long postId;
        private String title;
        private String content;
        private String regionName;
        private LocalDateTime createdAt;
        private List<ImageResponse> postImages;

        @Getter
        @Builder
        @AllArgsConstructor
        @NoArgsConstructor
        public static class ImageResponse {
            private Long id;
            private String originalName;
            private String imageUrl;
        }
    }

    // 글 수정
    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PostUpdateResponse {
        private Long postId;
        private String title;
        private String content;
        private String regionName;
        private boolean isAnonymous;
        private LocalDateTime updatedAt;
    }

    // 댓글 작성
    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReplyCreateResponse {

        private Long postId;
        private Long userId;
        private String content;
    }

    // 댓글 조회
    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReplyListResponse {

        private Long replyId;
        private String content;
        private LocalDateTime createdAt;
        private Long likeCount;
    }

    // 댓글 수정
    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReplyUpdateResponse {

        private Long userId;
        private Long replyId;
        private String content;
        private boolean isAnonymous;
        private LocalDateTime updatedAt;
    }
}
