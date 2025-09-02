package seasonton.youthPolicy.domain.post.converter;

import seasonton.youthPolicy.domain.post.domain.entity.Posts;
import seasonton.youthPolicy.domain.post.domain.entity.Reply;
import seasonton.youthPolicy.domain.post.dto.PostResponseDTO;
import seasonton.youthPolicy.domain.model.entity.Region;

public class PostConverter {

    // 글 작성
    public static PostResponseDTO.PostCreateResponse toPostCreateResponse(
            Posts post,
            Region region
    ) {
        return PostResponseDTO.PostCreateResponse.builder()
                .title(post.getTitle())
                .content(post.getContent())
                .isAnonymous(post.isAnonymous())
                .regionName(region.getRegionName())
                .build();
    }

    // 글 목록 조회
    public static PostResponseDTO.PostListResponse toPostListResponse(Posts post) {
        return PostResponseDTO.PostListResponse.builder()
                .postId(post.getId())
                .title(post.getTitle())
                .regionName(post.getRegion().getRegionName())
                .createdAt(post.getCreatedAt())
                .build();
    }

    // 글 상세 조회
    public static PostResponseDTO.PostDetailResponse toPostDetailResponse(Posts post) {
        return PostResponseDTO.PostDetailResponse.builder()
                .postId(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .regionName(post.getRegion().getRegionName())
                .createdAt(post.getCreatedAt())
                .build();
    }

    // 글 수정
    public static PostResponseDTO.PostUpdateResponse toPostUpdateResponse(Posts post) {
        return PostResponseDTO.PostUpdateResponse.builder()
                .postId(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .regionName(post.getRegion().getRegionName())
                .isAnonymous(post.isAnonymous())
                .updatedAt(post.getUpdatedAt()) // BaseEntity 에 updatedAt 있다고 가정
                .build();
    }

    // 댓글 작성
    public static PostResponseDTO.ReplyCreateResponse toReplyCreateResponse(Long userId, Posts post, String content) {
        return PostResponseDTO.ReplyCreateResponse.builder()
                .userId(userId)
                .postId(post.getId())
                .content(content)
                .build();
    }

    // 댓글 조회
    public static PostResponseDTO.ReplyListResponse toReplyListResponse(Reply reply) {
        return PostResponseDTO.ReplyListResponse.builder()
                .replyId(reply.getId())
                .content(reply.getContent())
                .createdAt(reply.getCreatedAt())
                .build();
    }

    // 댓글 수정
    public static PostResponseDTO.ReplyUpdateResponse toReplyUpdateResponse(Reply reply) {
        return PostResponseDTO.ReplyUpdateResponse.builder()
                .replyId(reply.getId())
                .content(reply.getContent())
                .isAnonymous(reply.isAnonymous())
                .updatedAt(reply.getUpdatedAt()) // BaseEntity에 updatedAt 있다고 가정
                .build();
    }

}
