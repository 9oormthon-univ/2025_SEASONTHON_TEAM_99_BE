package seasonton.youthPolicy.global.error.code.status;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SuccessStatus {

    // Common
    OK(HttpStatus.OK, "COMMON_200", "성공입니다."),

    // post
    POST_CREATE_SUCCESS(HttpStatus.CREATED, "POST_201", "게시글이 성공적으로 생성되었습니다."),
    POST_READ_SUCCESS(HttpStatus.OK, "POST_200", "게시글 목록 조회 성공"),
    POST_UPDATE_SUCCESS(HttpStatus.OK, "POST_200", "게시글 수정 성공"),
    POST_DELETE_SUCCESS(HttpStatus.NO_CONTENT, "POST_204", "게시글 삭제 성공"),
    POST_REPLY_CREATE_SUCCESS(HttpStatus.CREATED, "REPLY_201", "댓글이 성공적으로 생성되었습니다."),
    REPLY_READ_SUCCESS(HttpStatus.OK, "REPLY_200", "댓글 목록 조회 성공"),
    REPLY_UPDATE_SUCCESS(HttpStatus.OK, "REPLY_200", "댓글 수정 성공"),
    REPLY_DELETE_SUCCESS(HttpStatus.NO_CONTENT, "REPLY_204", "댓글 삭제 성공"),
    POST_LIKE_TOGGLE_SUCCESS(HttpStatus.OK, "LIKE_200", "해당 정책에 좋아요 표시 완료"),
    REPLY_LIKE_TOGGLE_SUCCESS(HttpStatus.OK, "LIKE_200", "해당 댓글에 좋아요 표시 완료"),
    VOTE_UPDATE_SUCCESS(HttpStatus.OK, "VOTE_200", "투표 수정 성공"),
    VOTE_READ_SUCCESS(HttpStatus.OK, "VOTE_200", "투표 조회 성공"),
    VOTE_DELETE_SUCCESS(HttpStatus.NO_CONTENT, "VOTE_204", "투표 삭제 성공"),

    // policy
    POLICY_READ_SUCCESS(HttpStatus.OK, "POLICY_200", "정책 조회 성공"),
    POLICY_STATUS_SUCCESS(HttpStatus.OK, "POLICY_200", "정책 상태 조회 성공"),
    REGION_READ_SUCCESS(HttpStatus.OK, "REGION_200", "지역 조회 성공"),
    POLICY_DETAIL_SUCCESS(HttpStatus.OK, "POLICY_200", "정책 상세 조회 성공"),
    POLICY_REPLY_CREATE_SUCCESS(HttpStatus.CREATED, "REPLY_201", "댓글 작성 성공"),
    POLICY_REPLY_READ_SUCCESS(HttpStatus.OK, "REPLY_200", "댓글 조회 성공"),
    POLICY_LIKE_TOGGLE_SUCCESS(HttpStatus.OK, "LIKE_200", "해당 정책에 좋아요 표시 완료"),
    POLICY_LIKE_COUNT_SUCCESS(HttpStatus.OK, "LIKE_200", "해당 정책의 좋아요 갯수 조회 완료"),
    POLICY_REPLY_UPDATE_SUCCESS(HttpStatus.OK, "REPLY_200", "정책 댓글 수정 성공"),
    POLICY_REPLY_DELETE_SUCCESS(HttpStatus.NO_CONTENT, "REPLY_204", "정책 댓글 삭제 성공"),
    POLICY_REPLY_SUMMARY_SUCCESS(HttpStatus.OK, "REPLY_200", "댓글 요약 성공"),
    POLICY_REPLY_FILTER_SUCCESS(HttpStatus.OK, "REPLY_200", "댓글 필터링 성공"),

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
