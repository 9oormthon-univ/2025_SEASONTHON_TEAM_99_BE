package seasonton.youthPolicy.global.error.code.status;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorStatus {

    // Common error
    _INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 에러, 관리자에게 문의 바랍니다."),
    _BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON400", "잘못된 요청입니다."),
    _UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON401", "인증이 필요합니다."),
    _FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON403", "금지된 요청입니다."),

    // test
    ILLEGAL_ARGUMENT_INPUT(HttpStatus.BAD_REQUEST, "TEST4001", "음수는 입력할 수 없습니다."),

    // user
    USER_NOT_FIND(HttpStatus.BAD_REQUEST, "USER_4001", "해당 유저를 찾을 수 없습니다."),

    // S3
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FILE4001", "파일 업로드에 실패했습니다."),
    FILE_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FILE4002", "파일 삭제에 실패했습니다."),

    // post
    POST_IMAGE_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "POST_4001", "이미지는 최대 3개까지만 업로드 가능합니다."),
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "POST_4002", "존재하지 않는 게시글입니다."),
    POST_FORBIDDEN(HttpStatus.FORBIDDEN, "POST_4003", "해당 게시글에 대한 권한이 없습니다."),
    REPLY_NOT_FOUND(HttpStatus.NOT_FOUND, "REPLY_4001", "존재하지 않는 댓글입니다."),
    REPLY_FORBIDDEN(HttpStatus.FORBIDDEN, "REPLY_4002", "해당 댓글에 대한 권한이 없습니다."),
    POST_LIKE_TOGGLE_SUCCESS(HttpStatus.OK, "LIKE_200", "게시글 좋아요 토글 성공"),


    // region
    REGION_NOT_FOUND(HttpStatus.NOT_FOUND, "REGION_4001", "존재하지 않는 지역입니다."),

    // policy
    POLICY_NOT_FOUND(HttpStatus.NOT_FOUND, "POLICY_4001", "존재하지 않는 정책입니다."),
    POLICY_FORBIDDEN(HttpStatus.FORBIDDEN, "POLICY_4002", "해당 정책에 대한 권한이 없습니다."),
    POLICY_INVALID_REQUEST(HttpStatus.BAD_REQUEST, "POLICY_4003", "잘못된 정책 요청입니다."),
    POLICY_API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "POLICY_4004", "정책 API 호출 중 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    public ErrorReasonDTO getReasonHttpStatus() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .httpStatus(httpStatus)
                .build();
    }
}
