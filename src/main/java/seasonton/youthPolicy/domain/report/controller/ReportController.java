package seasonton.youthPolicy.domain.report.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import seasonton.youthPolicy.domain.report.dto.ReportRequestDTO;
import seasonton.youthPolicy.domain.report.dto.ReportResponseDTO;
import seasonton.youthPolicy.domain.report.exception.ReportException;
import seasonton.youthPolicy.domain.report.service.ReportService;
import seasonton.youthPolicy.global.auth.UserPrincipal;
import seasonton.youthPolicy.global.common.response.BaseResponse;
import seasonton.youthPolicy.global.error.code.status.ErrorStatus;
import seasonton.youthPolicy.global.error.code.status.SuccessStatus;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;

    // 레포트 생성
    @PostMapping(value = "/new")
    @Operation(
            summary = "레포트 생성",
            description = "생성할 \"yyyy-mm\"/지역구를 request body 로 보내주시고, 운영진계정 확인을 위해 Jwt 토큰 인증을 해주세요."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "OK", description = "레포트 생성 성공"),
            @ApiResponse(responseCode = "REPORT_4001", description = "이미 존재하는 레포트"),
            @ApiResponse(responseCode = "REPORT_4002", description = "운영자 계정만 생성가능")
    }) public BaseResponse<ReportResponseDTO.ReportDetailResponse> reportCreate(
            @RequestBody ReportRequestDTO.ReportCreateDTO request,
            @AuthenticationPrincipal UserPrincipal userPrincipal){

        if (!userPrincipal.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))){
            throw new ReportException(ErrorStatus.REPORT_CREATE_NOAUTH);
        }
        ReportResponseDTO.ReportDetailResponse result = reportService.summarizePosts(request);
        return BaseResponse.onSuccess(SuccessStatus.OK, result);
    }

    @GetMapping(value = "/{report-id}")
    @Operation(summary = "레포트 상세조회", description = "report id를 path로 넘겨주세요")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "OK", description = "레포트 상세조회 성공"),
            @ApiResponse(responseCode = "REPORT_4003", description = "존재하지 않는 레포트")
    }) public BaseResponse<ReportResponseDTO.ReportDetailResponse> reportDetail(@PathVariable("report-id") Long reportId){
        ReportResponseDTO.ReportDetailResponse result = reportService.getDetailReport(reportId);
        return BaseResponse.onSuccess(SuccessStatus.OK, result);
    }

    @GetMapping(value = "/lists")
    @Operation(summary = "레포트 리스트조회", description = "모든 레포트를 최신순으로 조회합니다 (yearMonth 별로 묶어서 반환됩니다) (쿼리파라미터 추가할 경우, 조건별 필터링 됩니다)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "OK", description = "레포트 성공")
    }) public BaseResponse<Map<String, List<ReportResponseDTO.ReportListResponse>>> reportList(
            @Parameter(description = "시작 년월 (yyyy-MM)", example = "2025-01")
            @RequestParam(value = "from", required = false) String from,
            @Parameter(description = "종료 년월 (yyyy-MM)", example = "2025-09")
            @RequestParam(value = "to", required = false) String to,
            @Parameter(description = "지역 ID", example = "1")
            @RequestParam(value = "regionId", required = false) Long regionId
    ){
        YearMonth ymFrom = parseYearMonthNullable(from);
        YearMonth ymTo   = parseYearMonthNullable(to);

        if (ymFrom != null && ymTo != null && ymFrom.isAfter(ymTo)) {
            throw new IllegalArgumentException("from은 to보다 이후일 수 없습니다. (yyyy-MM)");
        }

        if ((ymFrom != null && ymTo == null) || (ymFrom == null && ymTo != null)) {
            throw new IllegalArgumentException("from과 to는 반드시 함께있어야 합니다.");
        }

        Map<String,List<ReportResponseDTO.ReportListResponse>> result = reportService.getListReport(ymFrom, ymTo, regionId);
        return BaseResponse.onSuccess(SuccessStatus.OK, result);
    }

    private static YearMonth parseYearMonthNullable(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return YearMonth.parse(s, DateTimeFormatter.ofPattern("yyyy-MM"));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("년월은 yyyy-MM 형식이어야 합니다. 입력값: " + s);
        }
    }

}
