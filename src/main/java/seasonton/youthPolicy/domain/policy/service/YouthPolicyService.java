package seasonton.youthPolicy.domain.policy.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import seasonton.youthPolicy.domain.policy.domain.entity.PolicyReply;
import seasonton.youthPolicy.domain.policy.domain.enums.PolicyStatus;
import seasonton.youthPolicy.domain.policy.domain.repository.PolicyReplyRepository;
import seasonton.youthPolicy.domain.policy.dto.PolicyRequestDTO;
import seasonton.youthPolicy.domain.policy.dto.PolicyResponseDTO;
import seasonton.youthPolicy.domain.policy.exception.PolicyException;
import seasonton.youthPolicy.domain.user.domain.entity.User;
import seasonton.youthPolicy.domain.user.domain.repository.UserRepository;
import seasonton.youthPolicy.global.common.RegionCodeMapper;
import seasonton.youthPolicy.global.error.code.status.ErrorStatus;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class YouthPolicyService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper; // JSON → DTO 변환용
    private final RegionCodeMapper regionCodeMapper;
    private final UserRepository userRepository;
    private final PolicyReplyRepository policyReplyRepository;

    @Value("${youth.api.url}")
    private String baseUrl;

    @Value("${youth.api.key}")
    private String apiKey;

    /*
    활용 할 정책 컬럼

    정책명 : plcyNm
    정책 거주지역 : zipCd
    정책 신청 주소 : aplyUrlAddr
    정책 설명 : plcyExplnCn
    정책 지원 내용 : plcySprtCn
    정책 지원 규모 : sprtSclLmtYn
    정책 신청 방법 : plcyAplyMthdCn
    지원대상최소연령 : sprtTrgtMinAge
    지원대상최대연령 : sprtTrgtMaxAge
    지원대상연령제한여부 : sprtTrgtAgeLmtYn
    정책학력요건코드 : schoolCd
    정책취업요건코드 : jobCd
    소득 조건 구분코드 : earnCndSeCd
    소득 최소 금액 : earnMinAmt
    소득 최대 금액 : earnMaxAmt
    소득 기타 내용 : earnEtcCn
    기타 조건 : addAplyQlfcCndCn
    제출서류내용 : sbmsnDcmntCn
    심사방법 : srngMthdCn
    최초등록일시: frstRegDt
    최종수정일시: lastMdfcnDt
    * */

    // 최신 순 보기
    public List<PolicyResponseDTO.YouthPolicyResponse> getPolicies(int pageNum, int pageSize) {
        try {
            String url = baseUrl
                    + "?apiKeyNm=" + apiKey
                    + "&rtnType=json"
                    + "&pageNum=" + pageNum
                    + "&pageSize=" + pageSize;

            String response = restTemplate.getForObject(url, String.class);
            if (response == null || response.isBlank()) {
                throw new PolicyException(ErrorStatus.POLICY_API_ERROR);
            }

            JsonNode root = objectMapper.readTree(response);
            JsonNode items = root.path("result").path("youthPolicyList");

            if (items.isMissingNode() || !items.isArray()) {
                throw new PolicyException(ErrorStatus.POLICY_NOT_FOUND);
            }

            List<PolicyResponseDTO.YouthPolicyResponse> results = new ArrayList<>();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            for (JsonNode item : items) {
                String zipCodes = item.path("zipCd").asText(null);
                List<String> regionNames = new ArrayList<>();
                if (zipCodes != null) {
                    for (String code : zipCodes.split(",")) {
                        regionNames.add(regionCodeMapper.getRegionName(code.trim()));
                    }
                }

                results.add(
                        PolicyResponseDTO.YouthPolicyResponse.builder()
                                .plcyNm(item.path("plcyNm").asText(null))
                                .regionNames(regionNames)
                                .frstRegDt(item.path("frstRegDt").asText(null)) // 정렬용 등록일
                                .build()
                );
            }

            // ✅ 최신순 정렬 (등록일 내림차순, 이름 사전순)
            results.sort(
                    Comparator.comparing(
                                    (PolicyResponseDTO.YouthPolicyResponse p) ->
                                            LocalDateTime.parse(p.getFrstRegDt(), formatter)
                            ).reversed()
                            .thenComparing(PolicyResponseDTO.YouthPolicyResponse::getPlcyNm,
                                    Comparator.nullsLast(String::compareTo))
            );

            if (results.isEmpty()) {
                throw new PolicyException(ErrorStatus.POLICY_NOT_FOUND);
            }

            // ✅ 수동 페이징
            int fromIndex = (pageNum - 1) * pageSize;
            int toIndex = Math.min(fromIndex + pageSize, results.size());
            if (fromIndex >= results.size()) {
                return Collections.emptyList();
            }

            return results.subList(fromIndex, toIndex);
        } catch (PolicyException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            throw new PolicyException(ErrorStatus.POLICY_INVALID_REQUEST);
        } catch (Exception e) {
            throw new PolicyException(ErrorStatus.POLICY_API_ERROR);
        }
    }

    // 정책 상태 조회
    public PolicyResponseDTO.PolicyStatusResponse getPolicyStatus(String plcyNo) {
        try {
            String url = baseUrl
                    + "?apiKeyNm=" + apiKey
                    + "&rtnType=json"
                    + "&plcyNo=" + plcyNo;

            String response = restTemplate.getForObject(url, String.class);
            if (response == null || response.isBlank()) {
                throw new PolicyException(ErrorStatus.POLICY_API_ERROR);
            }

            JsonNode root = objectMapper.readTree(response);
            JsonNode item = root.path("result").path("youthPolicyList").get(0);

            if (item == null || item.isMissingNode()) {
                throw new PolicyException(ErrorStatus.POLICY_NOT_FOUND);
            }

            String plcyNm = item.path("plcyNm").asText(null);          // ✅ 정책명
            String startDate = item.path("bizPrdBgngYmd").asText(null);
            String endDate = item.path("bizPrdEndYmd").asText(null);

            PolicyStatus status = calculateStatus(startDate, endDate);

            return PolicyResponseDTO.PolicyStatusResponse.builder()
                    .plcyNo(plcyNo)
                    .plcyNm(plcyNm)
                    .status(status)
                    .startDate(startDate == null || startDate.isBlank() ? "상시" : startDate)
                    .endDate(endDate == null || endDate.isBlank() ? "상시" : endDate)
                    .build();

        } catch (PolicyException e) {
            throw e;
        } catch (Exception e) {
            throw new PolicyException(ErrorStatus.POLICY_API_ERROR);
        }
    }

    // 정책 지역 정보 조회
    public PolicyResponseDTO.PolicyRegionResponse getPolicyRegionsByNo(String plcyNo) {
        try {
            String url = baseUrl
                    + "?apiKeyNm=" + apiKey
                    + "&rtnType=json"
                    + "&plcyNo=" + plcyNo; // ✅ 정책번호 조건 추가

            String response = restTemplate.getForObject(url, String.class);
            if (response == null || response.isBlank()) {
                throw new PolicyException(ErrorStatus.POLICY_API_ERROR);
            }

            JsonNode root = objectMapper.readTree(response);
            JsonNode items = root.path("result").path("youthPolicyList");

            if (items.isMissingNode() || !items.isArray() || items.isEmpty()) {
                throw new PolicyException(ErrorStatus.POLICY_NOT_FOUND);
            }

            JsonNode item = items.get(0); // 단일 정책 조회니까 첫 번째만 사용
            Set<String> regionSet = new HashSet<>();

            String zipCodes = item.path("zipCd").asText(null);
            if (zipCodes != null && !zipCodes.isBlank()) {
                String[] codes = zipCodes.split(",");

                for (String code : codes) {
                    String regionName = regionCodeMapper.getRegionName(code.trim());
                    if (regionName != null) {
                        String topLevelRegion = extractTopLevel(regionName); // ✅ 시/도 단위 추출
                        regionSet.add(topLevelRegion);
                    }
                }
            }

            return PolicyResponseDTO.PolicyRegionResponse.builder()
                    .regions(regionSet)
                    .build();

        } catch (PolicyException e) {
            throw e;
        } catch (Exception e) {
            throw new PolicyException(ErrorStatus.POLICY_API_ERROR);
        }
    }

    // 정책 상세보기
    public PolicyResponseDTO.YouthPolicyDetailResponse getPolicyDetailByName(String plcyNm) {
        try {
            String url = baseUrl
                    + "?apiKeyNm=" + apiKey
                    + "&rtnType=json"
                    + "&plcyNm=" + plcyNm;

            String response = restTemplate.getForObject(url, String.class);
            if (response == null || response.isBlank()) {
                throw new PolicyException(ErrorStatus.POLICY_API_ERROR);
            }

            JsonNode root = objectMapper.readTree(response);
            JsonNode items = root.path("result").path("youthPolicyList");

            if (items.isMissingNode() || !items.isArray() || items.isEmpty()) {
                throw new PolicyException(ErrorStatus.POLICY_NOT_FOUND);
            }

            JsonNode item = items.get(0);

            // 지역 변환
            List<String> regionNames = new ArrayList<>();
            String zipCodes = item.path("zipCd").asText(null);
            if (zipCodes != null && !zipCodes.isBlank()) {
                String[] codes = zipCodes.split(",");
                for (String code : codes) {
                    String regionName = regionCodeMapper.getRegionName(code.trim());
                    if (regionName != null) {
                        regionNames.add(regionName);
                    }
                }
            }

            return PolicyResponseDTO.YouthPolicyDetailResponse.builder()
                    .plcyNm(item.path("plcyNm").asText(null))
                    .regions(regionNames)
                    .aplyUrlAddr(item.path("aplyUrlAddr").asText(null))
                    .plcyExplnCn(item.path("plcyExplnCn").asText(null))
                    .plcySprtCn(item.path("plcySprtCn").asText(null))
                    .sprtSclLmtYn(item.path("sprtSclLmtYn").asText(null))
                    .plcyAplyMthdCn(item.path("plcyAplyMthdCn").asText(null))
                    .sprtTrgtMinAge(item.hasNonNull("sprtTrgtMinAge") ? item.get("sprtTrgtMinAge").asInt() : null)
                    .sprtTrgtMaxAge(item.hasNonNull("sprtTrgtMaxAge") ? item.get("sprtTrgtMaxAge").asInt() : null)
                    .sprtTrgtAgeLmtYn(item.path("sprtTrgtAgeLmtYn").asText(null))
                    .schoolCd(item.path("schoolCd").asText(null))
                    .jobCd(item.path("jobCd").asText(null))
                    .earnCndSeCd(item.path("earnCndSeCd").asText(null))
                    .earnMinAmt(item.hasNonNull("earnMinAmt") ? item.get("earnMinAmt").asInt() : null)
                    .earnMaxAmt(item.hasNonNull("earnMaxAmt") ? item.get("earnMaxAmt").asInt() : null)
                    .earnEtcCn(item.path("earnEtcCn").asText(null))
                    .addAplyQlfcCndCn(item.path("addAplyQlfcCndCn").asText(null))
                    .sbmsnDcmntCn(item.path("sbmsnDcmntCn").asText(null))
                    .srngMthdCn(item.path("srngMthdCn").asText(null))
                    .frstRegDt(item.path("frstRegDt").asText(null))
                    .lastMdfcnDt(item.path("lastMdfcnDt").asText(null))
                    .build();

        } catch (PolicyException e) {
            throw e;
        } catch (Exception e) {
            throw new PolicyException(ErrorStatus.POLICY_API_ERROR);
        }
    }


    /**
     * 지역명에서 시/도 단위만 추출
     * 예) "서울특별시 강남구" → "서울특별시"
     *     "경상북도 경산시" → "경상북도"
     */
    private String extractTopLevel(String fullRegionName) {
        if (fullRegionName == null) return null;

        // 띄어쓰기 기준 첫 단어 + "시"/"도" 단위까지만
        if (fullRegionName.startsWith("서울특별시")) return "서울특별시";
        if (fullRegionName.startsWith("부산광역시")) return "부산광역시";
        if (fullRegionName.startsWith("대구광역시")) return "대구광역시";
        if (fullRegionName.startsWith("인천광역시")) return "인천광역시";
        if (fullRegionName.startsWith("광주광역시")) return "광주광역시";
        if (fullRegionName.startsWith("대전광역시")) return "대전광역시";
        if (fullRegionName.startsWith("울산광역시")) return "울산광역시";
        if (fullRegionName.startsWith("세종특별자치시")) return "세종특별자치시";

        if (fullRegionName.startsWith("경기도")) return "경기도";
        if (fullRegionName.startsWith("강원도")) return "강원도";
        if (fullRegionName.startsWith("충청북도")) return "충청북도";
        if (fullRegionName.startsWith("충청남도")) return "충청남도";
        if (fullRegionName.startsWith("전라북도")) return "전라북도";
        if (fullRegionName.startsWith("전라남도")) return "전라남도";
        if (fullRegionName.startsWith("경상북도")) return "경상북도";
        if (fullRegionName.startsWith("경상남도")) return "경상남도";
        if (fullRegionName.startsWith("제주특별자치도")) return "제주특별자치도";

        // 못 찾으면 그대로 반환
        return fullRegionName.split(" ")[0];
    }


    //진행 상태 계산
    private PolicyStatus calculateStatus(String startDate, String endDate) {

        if (startDate == null || startDate.isBlank() ||
                endDate == null || endDate.isBlank()) {
            return PolicyStatus.IN_PROGRESS;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate today = LocalDate.now();
        LocalDate start = LocalDate.parse(startDate.trim(), formatter);
        LocalDate end = LocalDate.parse(endDate.trim(), formatter);

        if (today.isBefore(start)) return PolicyStatus.NOT_STARTED;
        else if (!today.isAfter(end)) return PolicyStatus.IN_PROGRESS;
        else return PolicyStatus.COMPLETED;
    }

    // 댓글 작성
    @Transactional
    public PolicyResponseDTO.Reply createReply(Long userId, PolicyRequestDTO.Create request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new PolicyException(ErrorStatus.USER_NOT_FIND));

        PolicyReply reply = PolicyReply.builder()
                .content(request.getContent())
                .isAnonymous(request.isAnonymous())
                .plcyNo(request.getPlcyNo())
                .plcyNm(request.getPlcyNm())
                .user(user)
                .build();

        policyReplyRepository.save(reply);

        return PolicyResponseDTO.Reply.builder()
                .id(reply.getId())
                .content(reply.getContent())
                .isAnonymous(reply.isAnonymous())
                .plcyNo(reply.getPlcyNo())
                .plcyNm(reply.getPlcyNm())
                .writer(reply.isAnonymous() ? "익명" : user.getNickname())
                .build();
    }

    // 댓글 조회
    public List<PolicyResponseDTO.Reply> getReplies(String plcyNo) {
        return policyReplyRepository.findByPlcyNo(plcyNo).stream()
                .map(reply -> PolicyResponseDTO.Reply.builder()
                        .id(reply.getId())
                        .content(reply.getContent())
                        .isAnonymous(reply.isAnonymous())
                        .plcyNo(reply.getPlcyNo())
                        .plcyNm(reply.getPlcyNm())
                        .writer(reply.isAnonymous() ? "익명" : reply.getUser().getNickname())
                        .build())
                .collect(Collectors.toList());
    }
}