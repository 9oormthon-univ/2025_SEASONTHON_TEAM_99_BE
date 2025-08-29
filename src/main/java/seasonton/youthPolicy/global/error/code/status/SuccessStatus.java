package seasonton.youthPolicy.global.error.code.status;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SuccessStatus {

    // Common
    OK(HttpStatus.OK, "COMMON_200", "성공입니다."),

    // test
    SUCCESS_TEST(HttpStatus.OK, "TEST_200", "테스트 완료");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    public ReasonDTO getReasonHttpStatus() {
        return ReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(true)
                .httpStatus(httpStatus)
                .build();
    }
}
