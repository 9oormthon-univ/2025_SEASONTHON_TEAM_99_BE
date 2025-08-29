package seasonton.youthPolicy.domain.test.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import seasonton.youthPolicy.domain.test.dto.TestRequestDTO;
import seasonton.youthPolicy.domain.test.dto.TestResponseDTO;
import seasonton.youthPolicy.domain.test.service.TestService;
import seasonton.youthPolicy.global.common.response.BaseResponse;
import seasonton.youthPolicy.global.error.code.status.SuccessStatus;

@RestController
@RequiredArgsConstructor
@RequestMapping("/test")
public class TestController {

    private final TestService testService;

    @PostMapping("/calculation")
    @Operation(
            summary = "팩토리얼",
            description = "값을 response body에 입력하세요."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "TEST_200", description = "OK, 테스트를 완료했습니다.")
    })
    public BaseResponse<TestResponseDTO.TestResponse> testApi(
            @RequestBody TestRequestDTO.TestRequest request) {

        TestResponseDTO.TestResponse result = testService.test(request);

        return BaseResponse.onSuccess(SuccessStatus.SUCCESS_TEST, result);
    }
}
