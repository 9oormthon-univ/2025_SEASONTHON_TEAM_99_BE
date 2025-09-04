package seasonton.youthPolicy.domain.post.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import seasonton.youthPolicy.domain.post.converter.PostConverter;
import seasonton.youthPolicy.domain.post.domain.entity.*;
import seasonton.youthPolicy.domain.post.domain.repository.*;
import seasonton.youthPolicy.domain.post.dto.PostRequestDTO;
import seasonton.youthPolicy.domain.post.dto.PostResponseDTO;
import seasonton.youthPolicy.domain.post.exception.PostException;
import seasonton.youthPolicy.domain.model.entity.Region;
import seasonton.youthPolicy.domain.member.domain.entity.User;
import seasonton.youthPolicy.domain.model.repository.RegionRepository;
import seasonton.youthPolicy.domain.member.domain.repository.UserRepository;
import seasonton.youthPolicy.domain.member.exception.UserException;
import seasonton.youthPolicy.global.dto.S3DTO;
import seasonton.youthPolicy.global.error.code.status.ErrorStatus;
import seasonton.youthPolicy.global.service.S3Service;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final S3Service s3Service;
    private final PostImageRepository postImageRepository;
    private final UserRepository userRepository;
    private final ReplyRepository replyRepository;
    private final RegionRepository regionRepository;
    private final PostLikeRepository postLikeRepository;
    private final ReplyLikeRepository replyLikeRepository;

    @Value("${minio.dir.post-image}")
    private String postDIr;

    // 글 작성
    @Transactional
    public PostResponseDTO.PostCreateResponse createPost(Long userId, String title, String content, Long regionId, boolean isAnonymous, List<MultipartFile> images) {

        // 유저 검증 및 정보 불러오기
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorStatus.USER_NOT_FIND));

        // regionId로 지역 정보 조회
        Region region = regionRepository.findById(regionId)
                .orElseThrow(() -> new PostException(ErrorStatus.REGION_NOT_FOUND));

        Posts post = Posts.builder()
                .title(title)
                .content(content)
                .isAnonymous(isAnonymous)
                .region(region)
                .user(user)
                .build();

        postRepository.save(post);

        savePostImages(images, post);

        return PostConverter.toPostCreateResponse(post, region);
    }

    // 글 목록 조회
    public List<PostResponseDTO.PostListResponse> getPosts() {
        return postRepository.findAllWithRegionOrderByCreatedAtDesc()
                .stream()
                .map(PostConverter::toPostListResponse) // 여기서 컨버터 사용
                .toList();

    }

    // 지역 기반 글 목록 조회
    public List<PostResponseDTO.PostRegionListResponse> getPostsByRegion(Long regionId) {
        Region region = regionRepository.findById(regionId)
                .orElseThrow(() -> new PostException(ErrorStatus.REGION_NOT_FOUND));

        return postRepository.findByRegion(region).stream()
                .map(post -> PostResponseDTO.PostRegionListResponse.builder()
                        .postId(post.getId())
                        .title(post.getTitle())
                        .regionName(region.getRegionName())
                        .createdAt(post.getCreatedAt())
                        .build())
                .toList();
    }

    // 글 상세 조회
    public PostResponseDTO.PostDetailResponse getDetailPost(Long postId) {
        Posts post = postRepository.findById(postId)
                .orElseThrow(() -> new PostException(ErrorStatus.POST_NOT_FOUND));

        return PostConverter.toPostDetailResponse(post);
    }

    // 댓글 작성
    @Transactional
    public PostResponseDTO.ReplyCreateResponse createReply(PostRequestDTO.ReplyCreateRequest request, Long userId, Long postId) {

        // 유저 정보 조회 및 검증
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorStatus.USER_NOT_FIND));

        // 댓글 달 게시글 조회 및 검증
        Posts post = postRepository.findById(postId)
                .orElseThrow(() -> new PostException(ErrorStatus.POST_NOT_FOUND));

        Reply reply = Reply.builder()
                .content(request.getContent())
                .isAnonymous(request.isAnonymous())
                .user(user)
                .post(post)
                .build();

        replyRepository.save(reply);

        return PostConverter.toReplyCreateResponse(userId, post, reply.getContent());
    }

    // 댓글 조회
    public List<PostResponseDTO.ReplyListResponse> getReplyList(Long postId) {
        List<Reply> replies = replyRepository.findAllByPostIdWithUser(postId);

        return replies.stream()
                .map(PostConverter::toReplyListResponse)
                .toList();
    }

    // 글 수정
    @Transactional
    public PostResponseDTO.PostUpdateResponse updatePost(String title, String content, Long regionId, boolean isAnonymous, Long postId,
                                                         Long userId, List<MultipartFile> newImages) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorStatus.USER_NOT_FIND));

        Posts post = postRepository.findById(postId)
                .orElseThrow(() -> new PostException(ErrorStatus.POST_NOT_FOUND));

        if (!post.getUser().getId().equals(userId)) {
            throw new PostException(ErrorStatus.POST_FORBIDDEN);
        }

        // 텍스트 수정
        Region region = regionId != null
                ? regionRepository.findById(regionId)
                .orElseThrow(() -> new PostException(ErrorStatus.REGION_NOT_FOUND))
                : post.getRegion();

        post.updatePost(title, content, isAnonymous, region);

        // 이미지 교체 로직
        if (newImages != null) {

            // 기존 이미지 삭제
            List<PostImage> oldImages = postImageRepository.findByPost(post);
            for (PostImage img : oldImages) {
                s3Service.deleteFile(img.getStoredName()); // S3 삭제
                postImageRepository.delete(img);           // DB 삭제
            }

            savePostImages(newImages, post);

        }

        return PostConverter.toPostUpdateResponse(post);
    }

    // 글 삭제
    @Transactional
    public void deletePost(Long postId, Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorStatus.USER_NOT_FIND));

        Posts post = postRepository.findById(postId)
                .orElseThrow(() -> new PostException(ErrorStatus.POST_NOT_FOUND));

        if (!post.getUser().getId().equals(userId)) {
            throw new PostException(ErrorStatus.POST_FORBIDDEN);
        }

        // 4. 이미지 삭제 (S3 + DB)
        List<PostImage> images = postImageRepository.findByPost(post);
        for (PostImage img : images) {
            s3Service.deleteFile(img.getStoredName()); // S3에서 삭제
            postImageRepository.delete(img);           // DB에서 삭제
        }

        // 5. 댓글 삭제 (CascadeType.REMOVE 안 걸려 있으면 수동 삭제)
        List<Reply> replies = replyRepository.findAllByPost(post);
        replyRepository.deleteAll(replies);

        // 6. 게시글 삭제
        postRepository.delete(post);
    }

    // 댓글 수정
    @Transactional
    public PostResponseDTO.ReplyUpdateResponse updateReply(Long replyId, Long userId, PostRequestDTO.ReplyUpdateRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorStatus.USER_NOT_FIND));

        Reply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new PostException(ErrorStatus.REPLY_NOT_FOUND));

        if (!reply.getUser().getId().equals(userId)) {
            throw new PostException(ErrorStatus.REPLY_FORBIDDEN);
        }

        // 수정
        reply.updateReply(request.getContent(), request.isAnonymous());

        return PostConverter.toReplyUpdateResponse(reply);
    }

    @Transactional
    public void deleteReply(Long replyId, Long userId) {
        // 유저 검증
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorStatus.USER_NOT_FIND));

        // 댓글 조회
        Reply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new PostException(ErrorStatus.REPLY_NOT_FOUND));

        // 작성자 검증
        if (!reply.getUser().getId().equals(userId)) {
            throw new PostException(ErrorStatus.REPLY_FORBIDDEN);
        }

        // 댓글 삭제
        replyRepository.delete(reply);
    }

    // 이미지 업로드 헬퍼 메서드
    private void savePostImages(List<MultipartFile> images, Posts post) {
        if (images == null || images.isEmpty()) return;

        // 최대 개수 제한
        if (images.size() > 3) {
            throw new PostException(ErrorStatus.POST_IMAGE_LIMIT_EXCEEDED);
        }

        for (MultipartFile image : images) {
            S3DTO.UploadResult result = s3Service.uploadFile(postDIr, image);

            PostImage postImage = PostImage.builder()
                    .originalName(result.getOriginalName())
                    .storedName(result.getStoredName())
                    .imageUrl(result.getUrl())
                    .post(post)
                    .build();

            postImageRepository.save(postImage);
        }
    }

    // 게시글 좋아요
    @Transactional
    public String togglePostLike(Long userId, Long postId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorStatus.USER_NOT_FIND));

        Posts post = postRepository.findById(postId)
                .orElseThrow(() -> new PostException(ErrorStatus.POST_NOT_FOUND));

        PostLike existing = postLikeRepository.findByUserAndPost(user, post)
                .orElse(null);

        if (existing != null) {
            postLikeRepository.delete(existing);
            return "게시글 좋아요 취소됨";
        } else {
            PostLike like = PostLike.builder()
                    .user(user)
                    .post(post)
                    .build();
            postLikeRepository.save(like);
            return "게시글 좋아요 추가됨";
        }
    }

    // 댓글 좋아요
    @Transactional
    public String toggleReplyLike(Long userId, Long replyId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorStatus.USER_NOT_FIND));

        Reply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new PostException(ErrorStatus.REPLY_NOT_FOUND));

        ReplyLike existing = replyLikeRepository.findByUserAndReply(user, reply).orElse(null);

        if (existing != null) {
            replyLikeRepository.delete(existing);
            return "댓글 좋아요 취소됨";
        } else {
            ReplyLike like = ReplyLike.builder()
                    .user(user)
                    .reply(reply)
                    .build();
            replyLikeRepository.save(like);
            return "댓글 좋아요 추가됨";
        }
    }
}