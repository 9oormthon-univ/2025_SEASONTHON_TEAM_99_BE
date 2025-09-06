package seasonton.youthPolicy.domain.post.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import seasonton.youthPolicy.domain.post.converter.PostConverter;
import seasonton.youthPolicy.domain.post.domain.entity.*;
import seasonton.youthPolicy.domain.post.domain.repository.*;
import seasonton.youthPolicy.domain.post.dto.PostRequestDTO;
import seasonton.youthPolicy.domain.post.dto.PostResponseDTO;
import seasonton.youthPolicy.domain.post.dto.VoteRequestDTO;
import seasonton.youthPolicy.domain.post.dto.VoteResponseDTO;
import seasonton.youthPolicy.domain.post.exception.PostException;
import seasonton.youthPolicy.domain.model.entity.Region;
import seasonton.youthPolicy.domain.member.domain.entity.User;
import seasonton.youthPolicy.domain.model.repository.RegionRepository;
import seasonton.youthPolicy.domain.member.domain.repository.UserRepository;
import seasonton.youthPolicy.domain.member.exception.UserException;
import seasonton.youthPolicy.global.dto.S3DTO;
import seasonton.youthPolicy.global.error.code.status.ErrorStatus;
import seasonton.youthPolicy.global.service.S3Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private final PostVoteRepository postVoteRepository;
    private final PostVoteOptionRepository postVoteOptionRepository;
    private final PostVoteRecordRepository postVoteRecordRepository;

    @Value("${minio.dir.post-image}")
    private String postDIr;

    // 글 작성
    @Transactional
    public PostResponseDTO.PostCreateResponse createPost(Long userId, String title, String content,
                                                         Long regionId, boolean isAnonymous,
                                                         List<MultipartFile> images,
                                                         String question, List<String> options,
                                                         LocalDateTime endDate, boolean multipleChoice) {

        // 유저 검증
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorStatus.USER_NOT_FIND));

        // 지역 검증
        Region region = regionRepository.findById(regionId)
                .orElseThrow(() -> new PostException(ErrorStatus.REGION_NOT_FOUND));

        // 게시글 저장
        Posts post = Posts.builder()
                .title(title)
                .content(content)
                .isAnonymous(isAnonymous)
                .writer(isAnonymous ? "익명" : user.getNickname())
                .region(region)
                .user(user)
                .build();

        postRepository.save(post);

        // 이미지 저장 (있을 때만)
        if (images != null && !images.isEmpty()) {
            savePostImages(images, post);
        }

        // 투표 저장 (질문/옵션이 있을 경우)
        if (question != null && !question.isBlank() && options != null && !options.isEmpty()) {
            PostVote vote = PostVote.builder()
                    .question(question)
                    .endDate(endDate)
                    .multipleChoice(multipleChoice)
                    .post(post)
                    .build();

            List<PostVoteOption> optionEntities = options.stream()
                    .map(opt -> PostVoteOption.builder()
                            .optionText(opt)
                            .vote(vote)
                            .build())
                    .toList();

            vote.getOptions().addAll(optionEntities);

            postVoteRepository.save(vote);
        }

        // 응답 반환
        return PostConverter.toPostCreateResponse(post, region);
    }

    // 투표 수정
    @Transactional
    public PostResponseDTO.PostUpdateResponse updateVote(Long postId, Long userId,
                                                         String question, List<String> options, LocalDateTime endDate, boolean multipleChoice) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorStatus.USER_NOT_FIND));

        Posts post = postRepository.findById(postId)
                .orElseThrow(() -> new PostException(ErrorStatus.POST_NOT_FOUND));

        if (!post.getUser().getId().equals(userId)) {
            throw new PostException(ErrorStatus.POST_FORBIDDEN);
        }

        PostVote vote = postVoteRepository.findByPost(post)
                .orElseThrow(() -> new PostException(ErrorStatus.VOTE_NOT_FOUND));

        List<PostVoteOption> newOptions = options.stream()
                .map(opt -> PostVoteOption.builder()
                        .optionText(opt)
                        .vote(vote)
                        .build())
                .toList();

        // 엔티티 메서드 호출
        vote.updateVote(question, endDate, multipleChoice, newOptions);

        postVoteRepository.save(vote);

        return PostConverter.toPostUpdateResponse(post);
    }

    // 투표 조회
    public VoteResponseDTO.PostVoteResponse getVote(Long postId) {
        Posts post = postRepository.findById(postId)
                .orElseThrow(() -> new PostException(ErrorStatus.POST_NOT_FOUND));

        PostVote vote = postVoteRepository.findByPost(post)
                .orElseThrow(() -> new PostException(ErrorStatus.VOTE_NOT_FOUND));

        List<VoteResponseDTO.PostVoteResponse.OptionResponse> optionResponses = vote.getOptions().stream()
                .map(opt -> VoteResponseDTO.PostVoteResponse.OptionResponse.builder()
                        .optionId(opt.getId())
                        .optionText(opt.getOptionText())
                        .voteCount(opt.getVoteCount())
                        .build())
                .toList();

        return VoteResponseDTO.PostVoteResponse.builder()
                .voteId(vote.getId())
                .question(vote.getQuestion())
                .multipleChoice(vote.isMultipleChoice())
                .endDate(vote.getEndDate())
                .options(optionResponses)
                .build();
    }

    // 투표 삭제
    @Transactional
    public void deleteVote(Long postId, Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorStatus.USER_NOT_FIND));

        Posts post = postRepository.findById(postId)
                .orElseThrow(() -> new PostException(ErrorStatus.POST_NOT_FOUND));

        if (!post.getUser().getId().equals(userId)) {
            throw new PostException(ErrorStatus.POST_FORBIDDEN);
        }

        PostVote vote = postVoteRepository.findByPost(post)
                .orElseThrow(() -> new PostException(ErrorStatus.VOTE_NOT_FOUND));

        // 삭제 (옵션은 cascade = ALL, orphanRemoval = true 이므로 자동 삭제)
        postVoteRepository.delete(vote);
    }

    // 투표하기
    @Transactional
    public VoteResponseDTO.PostVoteResponse vote(Long userId, VoteRequestDTO.VoteRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorStatus.USER_NOT_FIND));

        PostVote vote = postVoteRepository.findById(request.getVoteId())
                .orElseThrow(() -> new PostException(ErrorStatus.VOTE_NOT_FOUND));

        // 투표 마감 여부 확인
        if (vote.getEndDate() != null && vote.getEndDate().isBefore(LocalDateTime.now())) {
            throw new PostException(ErrorStatus.VOTE_EXPIRED);
        }

        // 단일 선택만 가능한데 여러 개 선택한 경우
        if (!vote.isMultipleChoice() && request.getOptionIds().size() > 1) {
            throw new PostException(ErrorStatus.VOTE_MULTIPLE_NOT_ALLOWED);
        }

        // 기존 투표 기록 조회 & 제거
        List<PostVoteRecord> existingRecords = postVoteRecordRepository.findAllByVoteAndUser(vote, user);
        for (PostVoteRecord record : existingRecords) {
            PostVoteOption oldOption = record.getOption();
            oldOption.decreaseVoteCount();
            postVoteOptionRepository.save(oldOption);
            postVoteRecordRepository.delete(record);
        }

        // 새 투표 반영
        List<VoteResponseDTO.PostVoteResponse.OptionResponse> optionResponses = new ArrayList<>();

        for (Long optionId : request.getOptionIds()) {
            PostVoteOption option = postVoteOptionRepository.findById(optionId)
                    .orElseThrow(() -> new PostException(ErrorStatus.VOTE_OPTION_INVALID));

            option.increaseVoteCount();
            postVoteOptionRepository.save(option);

            postVoteRecordRepository.save(
                    PostVoteRecord.builder()
                            .user(user)
                            .vote(vote)
                            .option(option)
                            .build()
            );

            optionResponses.add(
                    VoteResponseDTO.PostVoteResponse.OptionResponse.builder()
                            .optionId(option.getId())
                            .optionText(option.getOptionText())
                            .voteCount(option.getVoteCount())
                            .build()
            );
        }

        // 최종 응답 DTO 반환
        return VoteResponseDTO.PostVoteResponse.builder()
                .voteId(vote.getId())
                .question(vote.getQuestion())
                .multipleChoice(vote.isMultipleChoice())
                .endDate(vote.getEndDate())
                .options(optionResponses)
                .build();
    }



    // 글 목록 조회
    public Page<PostResponseDTO.PostListResponse> getPosts(Pageable pageable) {
        Page<Posts> postsPage = postRepository.findAllWithRegion(pageable);

        return postsPage.map(post -> {
            Long likeCnt = postLikeRepository.countByPostId(post.getId());
            return PostConverter.toPostListResponse(post, likeCnt);
        });
    }

    // 게시글 좋아요 순 목록 조회
    public Page<PostResponseDTO.PostLikeListResponse> getPostsOrderByLikeCount(Pageable pageable) {
        Page<Posts> postsPage = postRepository.findAllOrderByLikeCountDesc(pageable);

        return postsPage.map(post ->
                PostResponseDTO.PostLikeListResponse.builder()
                        .id(post.getId())
                        .title(post.getTitle())
                        .content(post.getContent())
                        .regionName(post.getRegion().getRegionName())
                        .likeCount((long) post.getPostLikes().size())
                        .createdAt(post.getCreatedAt())
                        .build()
        );
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
    public PostResponseDTO.ReplyCreateResponse createReply(PostRequestDTO.ReplyCreateRequest request, Long userId, Long postId, boolean isAnonymous) {

        // 유저 정보 조회 및 검증
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorStatus.USER_NOT_FIND));

        // 댓글 달 게시글 조회 및 검증
        Posts post = postRepository.findById(postId)
                .orElseThrow(() -> new PostException(ErrorStatus.POST_NOT_FOUND));

        Reply reply = Reply.builder()
                .content(request.getContent())
                .isAnonymous(isAnonymous)
                .writer(isAnonymous ? "익명" : user.getNickname())
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
                .map(reply -> {
                    Long likeCnt = replyLikeRepository.countByReplyId(reply.getId());
                    return PostConverter.toReplyListResponse(reply, likeCnt);
                }) // 여기서 컨버터 사용
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

        post.updatePost(title, content, isAnonymous, isAnonymous ? "익명" : user.getNickname(), region);

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
    public PostResponseDTO.ReplyUpdateResponse updateReply(Long replyId, Long userId, PostRequestDTO.ReplyUpdateRequest request, boolean isAnonymous) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorStatus.USER_NOT_FIND));

        Reply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new PostException(ErrorStatus.REPLY_NOT_FOUND));

        if (!reply.getUser().getId().equals(userId)) {
            throw new PostException(ErrorStatus.REPLY_FORBIDDEN);
        }

        // 수정
        reply.updateReply(request.getContent(), isAnonymous, isAnonymous ? "익명" : user.getNickname());

        return PostConverter.toReplyUpdateResponse(reply);
    }

    // 댓글 삭제
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

    // 게시글 좋아요 개수 카운트
    public Long getPostLikeCount(Long postId) {
        return postLikeRepository.countByPostId(postId);
    }


    // 게시글 댓글 좋아요
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

    // 게시글 댓글 좋아요 카운트
    public Long getReplyLikeCount(Long replyId) {
        return replyLikeRepository.countByReplyId(replyId);
    }

}