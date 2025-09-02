package seasonton.youthPolicy.domain.post.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import seasonton.youthPolicy.domain.post.dto.PostRequestDTO;
import seasonton.youthPolicy.domain.post.dto.PostResponseDTO;
import seasonton.youthPolicy.domain.post.service.PostService;
import seasonton.youthPolicy.global.common.response.BaseResponse;
import seasonton.youthPolicy.global.error.code.status.SuccessStatus;

import java.security.Principal;
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
            @RequestPart("imageFile") List<MultipartFile> imageFile,
            Authentication auth
    ) {
        var details = (java.util.Map<?, ?>) auth.getDetails();
        Long userId = (Long) details.get("userId");
        PostResponseDTO.PostCreateResponse response = postService.createPost(userId, title, content, regionId, isAnonymous, imageFile);
        return BaseResponse.onSuccess(SuccessStatus.POST_CREATE_SUCCESS, response);

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
            @PathVariable("post-id") Long postId,
            @RequestParam Long userId,
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam Long regionId,
            @RequestParam boolean isAnonymous,
            @RequestPart(value = "newImages", required = false) List<MultipartFile> newImages
    ) {
        PostResponseDTO.PostUpdateResponse response = postService.updatePost(title,content,regionId,isAnonymous, postId, userId, newImages);
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
            @RequestParam Long userId
    ) {
        postService.deletePost(postId, userId);
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
            @RequestParam Long userId,
            @RequestBody @Valid PostRequestDTO.ReplyCreateRequest request
    ) {
        PostResponseDTO.ReplyCreateResponse response = postService.createReply(request, userId, postId);
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
            @RequestParam Long userId,
            @RequestBody @Valid PostRequestDTO.ReplyUpdateRequest request
    ) {
        PostResponseDTO.ReplyUpdateResponse response = postService.updateReply(replyId, userId, request);
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
            @RequestParam Long userId
    ) {
        postService.deleteReply(replyId, userId);
        return BaseResponse.onSuccess(SuccessStatus.REPLY_DELETE_SUCCESS, "댓글이 삭제되었습니다.");
    }

}