package seasonton.youthPolicy.domain.policy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import seasonton.youthPolicy.domain.policy.domain.enums.EarnConditionCode;
import seasonton.youthPolicy.domain.policy.domain.enums.JobCode;
import seasonton.youthPolicy.domain.policy.domain.enums.PolicyStatus;
import seasonton.youthPolicy.domain.policy.domain.enums.SchoolCode;

import java.util.List;
import java.util.Set;

public class PolicyResponseDTO {

    // 최신 순 조회
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class YouthPolicyResponse {
        private String plcyNo;             // 정책 코드
        private String plcyNm;             // 정책명
        private Set<String> regionNames;   // 시/도 단위 지역
        private String frstRegDt;          // 등록일
        private String lclsfNm;            // 대분류명
        private Long likeCount;            // 좋아요 수
        private PolicyStatus status;       // 진행 상태
        private String startDate;          // 시작일
        private String endDate;            // 종료일
        private String bizPrdBgngYmd;
        private String bizPrdEndYmd;
    }

    // 좋아요 순 조회
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class YouthPolicyLikeResponse {
        private String plcyNo;
        private String plcyNm;
        private Set<String> regionNames;   // 시/도 단위 지역
        private String frstRegDt;          // 등록일
        private String lclsfNm;            // 대분류명
        private Long likeCount;            // 좋아요 수
        private PolicyStatus status;       // 진행 상태
        private String startDate;          // 시작일
        private String endDate;            // 종료일
        private String bizPrdBgngYmd;
        private String bizPrdEndYmd;
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
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class YouthPolicyDetailResponse {
        private String plcyNo;
        private String plcyNm;
        private Set<String> regions;

        private SchoolCode schoolCd;
        private JobCode jobCd;
        private EarnConditionCode earnCndSeCd;

        private String aplyUrlAddr;
        private String plcyExplnCn;
        private String plcySprtCn;
        private String sprtSclLmtYn;
        private String plcyAplyMthdCn;
        private Integer sprtTrgtMinAge;
        private Integer sprtTrgtMaxAge;
        private String sprtTrgtAgeLmtYn;
        private Integer earnMinAmt;
        private Integer earnMaxAmt;
        private String earnEtcCn;
        private String addAplyQlfcCndCn;
        private String sbmsnDcmntCn;
        private String srngMthdCn;
        private String frstRegDt;
        private String lastMdfcnDt;
        private String lclsfNm;
        private String aplyYmd;
        private String startDate;
        private String endDate;
        private String bizPrdBgngYmd;
        private String bizPrdEndYmd;
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

    // 정책 검색
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class YouthPolicySearchResponse {
        private String plcyNo;             // 정책 코드
        private String plcyNm;             // 정책명
        private Set<String> regionNames;   // 시/도 단위 지역
        private String frstRegDt;          // 등록일
        private String lclsfNm;            // 대분류명
        private Long likeCount;            // 좋아요 수
        private PolicyStatus status;       // 정책 진행 상태 (예정/진행중/종료/상시)
        private String startDate;          // 시작일
        private String endDate;            // 종료일
        private String bizPrdBgngYmd;
        private String bizPrdEndYmd;
    }

    // 총 불러온 정책 수 카운트
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PolicyListResponse {
        private int totalCount;
        private List<PolicyResponseDTO.YouthPolicyResponse> policies; // 정책 리스트
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PolicyLikeListResponse {
        private int totalCount;
        private List<PolicyResponseDTO.YouthPolicyLikeResponse> policies; // 정책 리스트
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PolicySearchListResponse {
        private int totalCount;   // 검색된 정책 수
        private List<PolicyResponseDTO.YouthPolicySearchResponse> policies;
    }


}