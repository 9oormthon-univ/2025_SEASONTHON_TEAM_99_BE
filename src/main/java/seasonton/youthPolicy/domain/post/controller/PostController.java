package seasonton.youthPolicy.domain.post.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import seasonton.youthPolicy.domain.post.domain.entity.Posts;
import seasonton.youthPolicy.domain.post.dto.PostRequestDTO;
import seasonton.youthPolicy.domain.post.dto.PostResponseDTO;
import seasonton.youthPolicy.domain.post.dto.VoteRequestDTO;
import seasonton.youthPolicy.domain.post.dto.VoteResponseDTO;
import seasonton.youthPolicy.domain.post.service.PostService;
import seasonton.youthPolicy.global.auth.UserPrincipal;
import seasonton.youthPolicy.global.common.response.BaseResponse;
import seasonton.youthPolicy.global.error.code.status.SuccessStatus;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;

    // 글 작성
    @PostMapping(value = "/new", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "게시글 작성",
            description = "게시글 정보와 지역 정보를 multipart/form-data로 넘겨주시고, Jwt 토큰 인증을 해주세요."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "POST_201", description = "게시글 작성 성공"),
            @ApiResponse(responseCode = "USER_4001", description = "존재하지 않는 유저")
    })
    public BaseResponse<PostResponseDTO.PostCreateResponse> createPost(
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam Long regionId,
            @RequestParam boolean isAnonymous,
            @RequestPart(value = "imageFile", required = false) List<MultipartFile> imageFile,
            @RequestParam(required = false) String question,
            @RequestParam(required = false) List<String> options,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endDate,
            @RequestParam(required = false, defaultValue = "false") boolean multipleChoice,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        PostResponseDTO.PostCreateResponse response = postService.createPost(
                userPrincipal.getId(), title, content, regionId, isAnonymous,
                imageFile, question, options, endDate, multipleChoice);

        return BaseResponse.onSuccess(SuccessStatus.POST_CREATE_SUCCESS, response);
    }

    // 투표 수정
    @PatchMapping("/{postId}/vote")
    @Operation(
            summary = "투표 수정",
            description = "특정 게시글에 연결된 투표를 수정합니다. 작성자만 수정할 수 있습니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "VOTE_200", description = "투표 수정 성공"),
            @ApiResponse(responseCode = "USER_4001", description = "존재하지 않는 유저"),
            @ApiResponse(responseCode = "POST_4004", description = "존재하지 않는 게시글"),
            @ApiResponse(responseCode = "VOTE_4004", description = "존재하지 않는 투표"),
            @ApiResponse(responseCode = "POST_403", description = "게시글 작성자가 아님")
    })
    public BaseResponse<PostResponseDTO.PostUpdateResponse> updateVote(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam String question,
            @RequestParam List<String> options,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endDate,
            @RequestParam boolean multipleChoice
    ) {
        PostResponseDTO.PostUpdateResponse response = postService.updateVote(
                postId,
                userPrincipal.getId(),
                question,
                options,
                endDate,
                multipleChoice
        );

        return BaseResponse.onSuccess(SuccessStatus.VOTE_UPDATE_SUCCESS, response);
    }

    // 투표 조회
    @GetMapping("/{postId}/vote")
    @Operation(
            summary = "투표 조회",
            description = "특정 게시글에 연결된 투표 정보를 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "VOTE_200", description = "투표 조회 성공"),
            @ApiResponse(responseCode = "POST_4004", description = "존재하지 않는 게시글"),
            @ApiResponse(responseCode = "VOTE_4004", description = "존재하지 않는 투표")
    })
    public BaseResponse<VoteResponseDTO.PostVoteResponse> getVote(@PathVariable Long postId) {
        VoteResponseDTO.PostVoteResponse response = postService.getVote(postId);
        return BaseResponse.onSuccess(SuccessStatus.VOTE_READ_SUCCESS, response);
    }

    // 투표 삭제
    @DeleteMapping("/{postId}/vote")
    @Operation(
            summary = "투표 삭제",
            description = "특정 게시글에 연결된 투표를 삭제합니다. 작성자만 삭제할 수 있습니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "VOTE_204", description = "투표 삭제 성공"),
            @ApiResponse(responseCode = "USER_4001", description = "존재하지 않는 유저"),
            @ApiResponse(responseCode = "POST_4004", description = "존재하지 않는 게시글"),
            @ApiResponse(responseCode = "VOTE_4004", description = "존재하지 않는 투표"),
            @ApiResponse(responseCode = "POST_403", description = "게시글 작성자가 아님")
    })
    public BaseResponse<Void> deleteVote(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        postService.deleteVote(postId, userPrincipal.getId());
        return BaseResponse.onSuccess(SuccessStatus.VOTE_DELETE_SUCCESS, null);
    }


    // 글 목록 조회
    @GetMapping("/list")
    @Operation(
            summary = "게시글 목록 조회",
            description = "모든 게시글을 최신순(createdAt DESC)으로 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "POST_200", description = "게시글 목록 조회 성공")
    })
    public BaseResponse<List<PostResponseDTO.PostListResponse>> getPosts() {

        List<PostResponseDTO.PostListResponse> response = postService.getPosts();
        return BaseResponse.onSuccess(SuccessStatus.POST_READ_SUCCESS, response);
    }

    // 지역 기반 글 목록 조회
    @GetMapping("/region/{region-id}")
    @Operation(summary = "지역별 게시글 조회", description = "특정 지역에 속한 게시글 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "POST_200", description = "지역별 게시글 조회 성공"),
            @ApiResponse(responseCode = "REGION_4001", description = "존재하지 않는 지역")
    })
    public BaseResponse<List<PostResponseDTO.PostRegionListResponse>> getPostsByRegion(
            @PathVariable("region-id") Long regionId
    ) {
        List<PostResponseDTO.PostRegionListResponse> posts = postService.getPostsByRegion(regionId);
        return BaseResponse.onSuccess(SuccessStatus.POST_READ_SUCCESS, posts);
    }


    // 글 상세 조회
    @GetMapping("/{post-id}")
    @Operation(
            summary = "게시글 상세 조회",
            description = "postId를 Path로 넘겨주세요."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "POST_200", description = "게시글 상세 조회 성공"),
            @ApiResponse(responseCode = "POST_4002", description = "존재하지 않는 게시글")
    })
    public BaseResponse<PostResponseDTO.PostDetailResponse> getDetailPost(
            @PathVariable("post-id") Long postId
    ) {
        PostResponseDTO.PostDetailResponse response = postService.getDetailPost(postId);
        return BaseResponse.onSuccess(SuccessStatus.POST_READ_SUCCESS, response);
    }

    // 글 수정
    @PatchMapping(value = "/{post-id}/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "게시글 수정", description = "게시글 내용을 수정합니다. (본인 작성 글만 수정 가능)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "POST_200", description = "게시글 수정 성공"),
            @ApiResponse(responseCode = "POST_4002", description = "존재하지 않는 게시글"),
            @ApiResponse(responseCode = "POST_4003", description = "수정 권한이 없습니다"),
            @ApiResponse(responseCode = "POST_4001", description = "이미지는 최대 3개까지만 업로드 가능합니다"),
            @ApiResponse(responseCode = "REGION_4001", description = "존재하지 않는 지역")
    })
    public BaseResponse<PostResponseDTO.PostUpdateResponse> updatePost(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable("post-id") Long postId,
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam Long regionId,
            @RequestParam boolean isAnonymous,
            @RequestPart(value = "newImages", required = false) List<MultipartFile> newImages
    ) {
        PostResponseDTO.PostUpdateResponse response = postService.updatePost(
                title,content,regionId,isAnonymous, postId, userPrincipal.getId(), newImages);

        return BaseResponse.onSuccess(SuccessStatus.POST_UPDATE_SUCCESS, response);
    }

    // 글 삭제
    @DeleteMapping("/{post-id}/delete")
    @Operation(summary = "게시글 삭제", description = "게시글을 삭제합니다. (본인 작성 글만 삭제 가능)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "POST_204", description = "게시글 삭제 성공"),
            @ApiResponse(responseCode = "POST_4002", description = "존재하지 않는 게시글"),
            @ApiResponse(responseCode = "POST_4003", description = "삭제 권한이 없습니다")
    })
    public BaseResponse<String> deletePost(
            @PathVariable("post-id") Long postId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        postService.deletePost(postId, userPrincipal.getId());
        return BaseResponse.onSuccess(SuccessStatus.POST_DELETE_SUCCESS, "게시글이 삭제되었습니다.");
    }

    // 댓글 작성
    @PostMapping("/{post-id}/replies")
    @Operation(
            summary = "댓글 작성",
            description = "postId를 path에 포함하시고, 댓글에 넣을 정보를 request body에 담아서 넘겨주세요"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "REPLY_201", description = "댓글 작성 성공"),
            @ApiResponse(responseCode = "POST_4002", description = "존재하지 않는 게시글"),
            @ApiResponse(responseCode = "USER_4001", description = "존재하지 않는 유저")
    })
    public BaseResponse<PostResponseDTO.ReplyCreateResponse> createReply(
            @PathVariable("post-id") Long postId,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam boolean isAnonymous,
            @RequestBody @Valid PostRequestDTO.ReplyCreateRequest request
    ) {
        PostResponseDTO.ReplyCreateResponse response = postService.createReply(request, userPrincipal.getId(), postId, isAnonymous);
        return BaseResponse.onSuccess(SuccessStatus.POST_REPLY_CREATE_SUCCESS, response);
    }

    // 댓글 조회
    @GetMapping("/{post-id}/replies")
    @Operation(
            summary = "댓글 목록 조회",
            description = "특정 게시글(postId)에 달린 모든 댓글을 작성된 순서대로(createdAt ASC) 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "REPLY_200", description = "댓글 목록 조회 성공"),
            @ApiResponse(responseCode = "POST_4002", description = "존재하지 않는 게시글")
    })
    public BaseResponse<List<PostResponseDTO.ReplyListResponse>> getReplyList(
            @PathVariable("post-id") Long postId
    ) {
        List<PostResponseDTO.ReplyListResponse> response = postService.getReplyList(postId);
        return BaseResponse.onSuccess(SuccessStatus.REPLY_READ_SUCCESS, response);
    }

    // 댓글 수정
    @PatchMapping("/{reply-id}/replies")
    @Operation(summary = "댓글 수정", description = "특정 댓글 내용을 수정합니다. (작성자 본인만 수정 가능)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "REPLY_200", description = "댓글 수정 성공"),
            @ApiResponse(responseCode = "REPLY_4001", description = "존재하지 않는 댓글"),
            @ApiResponse(responseCode = "REPLY_4002", description = "수정 권한이 없습니다")
    })
    public BaseResponse<PostResponseDTO.ReplyUpdateResponse> updateReply(
            @PathVariable("reply-id") Long replyId,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam boolean isAnonymous,
            @RequestBody @Valid PostRequestDTO.ReplyUpdateRequest request
    ) {
        PostResponseDTO.ReplyUpdateResponse response = postService.updateReply(replyId, userPrincipal.getId(), request, isAnonymous);
        return BaseResponse.onSuccess(SuccessStatus.REPLY_UPDATE_SUCCESS, response);
    }


    // 댓글 삭제
    @DeleteMapping("/replies/{reply-id}/delete")
    @Operation(summary = "댓글 삭제", description = "댓글을 삭제합니다. (본인 작성 댓글만 삭제 가능)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "REPLY_2002", description = "댓글 삭제 성공"),
            @ApiResponse(responseCode = "REPLY_4001", description = "존재하지 않는 댓글"),
            @ApiResponse(responseCode = "REPLY_4002", description = "삭제 권한이 없습니다")
    })
    public BaseResponse<String> deleteReply(
            @PathVariable("reply-id") Long replyId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        postService.deleteReply(replyId, userPrincipal.getId());
        return BaseResponse.onSuccess(SuccessStatus.REPLY_DELETE_SUCCESS, "댓글이 삭제되었습니다.");
    }

    // 게시글 좋아요
    @PostMapping("/{post-id}/like")
    @Operation(summary = "게시글 좋아요 토글", description = "특정 게시글에 대해 좋아요를 추가하거나 취소합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "LIKE_200", description = "좋아요 토글 성공"),
            @ApiResponse(responseCode = "USER_4001", description = "존재하지 않는 유저"),
            @ApiResponse(responseCode = "POST_4001", description = "존재하지 않는 게시글")
    })
    public BaseResponse<String> togglePostLike(
            @PathVariable("post-id") Long postId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        String message = postService.togglePostLike(userPrincipal.getId(), postId);
        return BaseResponse.onSuccess(SuccessStatus.POST_LIKE_TOGGLE_SUCCESS, message);
    }

    // 댓글 좋아요
    @PostMapping("/{reply-id}/like/replies")
    @Operation(summary = "댓글 좋아요 토글", description = "특정 댓글에 대해 좋아요를 추가하거나 취소합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "LIKE_200", description = "좋아요 토글 성공"),
            @ApiResponse(responseCode = "USER_4001", description = "존재하지 않는 유저"),
            @ApiResponse(responseCode = "REPLY_4001", description = "존재하지 않는 댓글")
    })
    public BaseResponse<String> toggleReplyLike(
            @PathVariable("reply-id") Long replyId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        String message = postService.toggleReplyLike(userPrincipal.getId(), replyId);
        return BaseResponse.onSuccess(SuccessStatus.REPLY_LIKE_TOGGLE_SUCCESS, message);
    }
}