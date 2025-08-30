package seasonton.youthPolicy.domain.post.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
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

    @PostMapping(value = "/{user-id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
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
            @PathVariable("user-id") Long userId
    ) {
        PostResponseDTO.PostCreateResponse response = postService.createPost(userId, title, content, regionId, isAnonymous, imageFile);
        return BaseResponse.onSuccess(SuccessStatus.POST_CREATE_SUCCESS, response);

    }

}