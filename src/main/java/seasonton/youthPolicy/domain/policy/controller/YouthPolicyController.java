package seasonton.youthPolicy.domain.policy.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import seasonton.youthPolicy.domain.policy.dto.PolicyRequestDTO;
import seasonton.youthPolicy.domain.policy.dto.PolicyResponseDTO;
import seasonton.youthPolicy.domain.policy.service.YouthPolicyService;
import seasonton.youthPolicy.global.common.response.BaseResponse;
import seasonton.youthPolicy.global.error.code.status.SuccessStatus;
import seasonton.youthPolicy.global.auth.UserPrincipal;

import java.util.List;

@RestController
@RequestMapping("/youth")
@RequiredArgsConstructor
public class YouthPolicyController {

    private final YouthPolicyService youthPolicyService;

    // 정책 목록 최신순 조회
    @GetMapping("/policies")
    @Operation(
            summary = "정책 목록 최신순 조회",
            description = "정책 제목과 거주지역 코드만 반환합니다. " +
                    "최신 등록일 기준으로 정렬되며, 등록일이 같을 경우 정책명이 가나다순으로 정렬됩니다. " +
                    "한 페이지에 10개씩 페이징됩니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "정책 목록 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (PolicyException.POLICY_INVALID_REQUEST)"),
            @ApiResponse(responseCode = "404", description = "정책을 찾을 수 없음 (PolicyException.POLICY_NOT_FOUND)"),
            @ApiResponse(responseCode = "500", description = "정책 API 호출 실패 (PolicyException.POLICY_API_ERROR)")
    })
    public BaseResponse<List<PolicyResponseDTO.YouthPolicyResponse>> getPolicies(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {

        List<PolicyResponseDTO.YouthPolicyResponse> data = youthPolicyService.getPolicies(pageNum, pageSize);
        return BaseResponse.onSuccess(SuccessStatus.POLICY_READ_SUCCESS, data);
    }

    // 정책 진행 상태 조회
    @GetMapping("/policies/{plcy-no}/status")
    @Operation(summary = "정책 진행 상태 조회", description = "정책의 진행 상태(진행전/진행중/완료)를 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정책 상태 조회 성공"),
            @ApiResponse(responseCode = "404", description = "정책을 찾을 수 없음")
    })
    public BaseResponse<PolicyResponseDTO.PolicyStatusResponse> getPolicyStatus(
            @PathVariable("plcy-no") String plcyNo) {
        PolicyResponseDTO.PolicyStatusResponse status = youthPolicyService.getPolicyStatus(plcyNo);
        return BaseResponse.onSuccess(SuccessStatus.POLICY_STATUS_SUCCESS, status);
    }

    // 정책 지역 단일 조회
    @GetMapping("/policies/{plcyNo}/regions")
    @Operation(summary = "정책 지역 단일 조회", description = "정책번호로 해당 정책의 시행 지역(시/도 단위)을 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정책 지역 조회 성공"),
            @ApiResponse(responseCode = "404", description = "정책을 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "정책 API 호출 오류")
    })
    public BaseResponse<PolicyResponseDTO.PolicyRegionResponse> getPolicyRegionsByNo(
            @PathVariable String plcyNo) {

        PolicyResponseDTO.PolicyRegionResponse data = youthPolicyService.getPolicyRegionsByNo(plcyNo);
        return BaseResponse.onSuccess(SuccessStatus.REGION_READ_SUCCESS, data);
    }

    // 정책 상세 조회
    @GetMapping("/policies/detail")
    @Operation(
            summary = "정책 상세 조회",
            description = "정책 제목(plcyNm)으로 정책 상세 정보를 조회합니다. "
                    + "정책명은 공공데이터 API에 등록된 정확한 값과 일치해야 합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정책 상세 조회 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 정책"),
            @ApiResponse(responseCode = "500", description = "정책 API 호출 오류")
    })
    public BaseResponse<PolicyResponseDTO.YouthPolicyDetailResponse> getPolicyDetail(
            @RequestParam String plcyNm) {

        PolicyResponseDTO.YouthPolicyDetailResponse data = youthPolicyService.getPolicyDetailByName(plcyNm);
        return BaseResponse.onSuccess(SuccessStatus.POLICY_DETAIL_SUCCESS, data);
    }

    // 댓글 작성
    @PostMapping("/policies/create")
    @Operation(summary = "정책 댓글 작성", description = "특정 정책에 댓글을 작성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "댓글 작성 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 유저"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public BaseResponse<PolicyResponseDTO.Reply> PolicyCreateReply(
            @AuthenticationPrincipal UserPrincipal userPrincipal ,
            @RequestBody PolicyRequestDTO.Create request) {
        return BaseResponse.onSuccess(
                SuccessStatus.POLICY_REPLY_CREATE_SUCCESS,
                youthPolicyService.createReply(userPrincipal.getId(), request)
        );
    }

    // 댓글 조회
    @GetMapping("/policies/reply-list")
    @Operation(summary = "정책 댓글 조회", description = "정책 번호(plcyNo)로 해당 정책의 댓글을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "댓글 조회 성공"),
            @ApiResponse(responseCode = "404", description = "댓글 없음"),
    })
    public BaseResponse<List<PolicyResponseDTO.Reply>> getReplies(@RequestParam String plcyNo) {
        return BaseResponse.onSuccess(
                SuccessStatus.REPLY_READ_SUCCESS,
                youthPolicyService.getReplies(plcyNo)
        );
    }

    // 댓글 수정
    @PatchMapping("/policies/replies/{reply-id}")
    @Operation(summary = "정책 댓글 수정", description = "작성한 댓글을 수정합니다.")
    public BaseResponse<PolicyResponseDTO.ReplyUpdateResponse> updateReply(
            @PathVariable("reply-id") Long replyId,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody PolicyRequestDTO.ReplyUpdateRequest request) {
        return BaseResponse.onSuccess(
                SuccessStatus.POLICY_REPLY_UPDATE_SUCCESS,
                youthPolicyService.updateReply(replyId, userPrincipal.getId(), request)
        );
    }

    // 댓글 삭제
    @DeleteMapping("/policies/replies/{reply-id}")
    @Operation(summary = "정책 댓글 삭제", description = "작성한 댓글을 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "댓글 삭제 성공"),
            @ApiResponse(responseCode = "403", description = "삭제 권한 없음"),
            @ApiResponse(responseCode = "404", description = "댓글 없음"),
    })
    public BaseResponse<PolicyResponseDTO.ReplyDeleteResponse> deleteReply(
            @PathVariable("reply-id") Long replyId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return BaseResponse.onSuccess(
                SuccessStatus.POLICY_REPLY_DELETE_SUCCESS,
                youthPolicyService.deleteReply(replyId, userPrincipal.getId())
        );
    }


    // 정책 좋아요 토글 (추가/취소)
    @PostMapping("/{plcy-no}/like")
    @Operation(summary = "정책 좋아요 토글", description = "특정 정책에 대해 좋아요를 추가하거나 취소합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "좋아요 토글 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 유저")
    })
    public BaseResponse<String> toggleLike(
            @PathVariable("plcy-no") String plcyNo,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        String message = youthPolicyService.toggleLike(userPrincipal.getId(), plcyNo);
        return BaseResponse.onSuccess(SuccessStatus.LIKE_TOGGLE_SUCCESS, message);
    }

    // 정책 좋아요 갯수 조회
    @GetMapping("/{plcy-no}/likes")
    @Operation(summary = "정책 좋아요 수 조회", description = "특정 정책의 좋아요 개수를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "좋아요 수 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    public BaseResponse<Long> getLikeCount(@PathVariable("plcy-no") String plcyNo) {
        long count = youthPolicyService.getLikeCount(plcyNo);
        return BaseResponse.onSuccess(SuccessStatus.LIKE_COUNT_SUCCESS, count);
    }
}