package seasonton.youthPolicy.domain.policy.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import seasonton.youthPolicy.domain.policy.dto.PolicyRequestDTO;
import seasonton.youthPolicy.domain.policy.dto.PolicyResponseDTO;
import seasonton.youthPolicy.domain.policy.service.YouthPolicyService;
import seasonton.youthPolicy.global.common.response.BaseResponse;
import seasonton.youthPolicy.global.error.code.status.SuccessStatus;

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
            @RequestParam Long userId,
            @RequestBody PolicyRequestDTO.Create request) {
        return BaseResponse.onSuccess(
                SuccessStatus.POLICY_REPLY_CREATE_SUCCESS,
                youthPolicyService.createReply(userId, request)
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

}