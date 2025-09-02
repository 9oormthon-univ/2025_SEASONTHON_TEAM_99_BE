package seasonton.youthPolicy.domain.policy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import seasonton.youthPolicy.domain.policy.domain.enums.PolicyStatus;

import java.util.List;
import java.util.Set;

public class PolicyResponseDTO {

    // 최신 순 보기
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class YouthPolicyResponse {
        private String plcyNo;             // 정책 코드
        private String plcyNm;
        private List<String> regionNames;
        private String frstRegDt;
    }

    // 정책 진행 상태
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PolicyStatusResponse {
        private String plcyNm;
        private String plcyNo;
        private PolicyStatus status;
        private String startDate;
        private String endDate;
    }

    // 정책 지역 정보
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PolicyRegionResponse {
        private Set<String> regions; // 중복 제거된 시·도 단위 지역명
    }

    // 정책 상세 보기
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class YouthPolicyDetailResponse {

        private String plcyNo;             // 정책 코드
        private String plcyNm;             // 정책명
        private List<String> regions;      // 변환된 지역명 리스트
        private String aplyUrlAddr;        // 신청 주소
        private String plcyExplnCn;        // 정책 설명
        private String plcySprtCn;         // 지원 내용
        private String sprtSclLmtYn;       // 지원 규모 제한 여부
        private String plcyAplyMthdCn;     // 신청 방법
        private Integer sprtTrgtMinAge;    // 최소 연령
        private Integer sprtTrgtMaxAge;    // 최대 연령
        private String sprtTrgtAgeLmtYn;   // 연령 제한 여부
        private String schoolCd;           // 학력 요건
        private String jobCd;              // 취업 요건
        private String earnCndSeCd;        // 소득 조건 구분
        private Integer earnMinAmt;        // 소득 최소 금액
        private Integer earnMaxAmt;        // 소득 최대 금액
        private String earnEtcCn;          // 소득 기타 조건
        private String addAplyQlfcCndCn;   // 추가 조건
        private String sbmsnDcmntCn;       // 제출 서류
        private String srngMthdCn;         // 심사 방법
        private String frstRegDt;          // 최초 등록일시
        private String lastMdfcnDt;        // 최종 수정일시
    }

    // 정책 댓글 조회
    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Reply {
        private Long id;
        private String content;
        private boolean isAnonymous;
        private String plcyNo;
        private String plcyNm;
        private String writer;  // 유저 닉네임 or ID
    }

    // 정책 댓글 수정
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReplyUpdateResponse {
        private Long id;
        private String content;
        private boolean isAnonymous;
        private String plcyNo;
        private String plcyNm;
        private String writer;
    }

    // 정책 댓글 삭제
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReplyDeleteResponse {
        private Long id;          // 삭제된 댓글 ID
        private String message;   // 삭제 성공 메시지
    }


}
