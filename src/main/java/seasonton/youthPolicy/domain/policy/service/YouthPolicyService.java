package seasonton.youthPolicy.domain.policy.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import seasonton.youthPolicy.domain.member.domain.entity.User;
import seasonton.youthPolicy.domain.member.domain.repository.UserRepository;
import seasonton.youthPolicy.domain.policy.domain.entity.PolicyLike;
import seasonton.youthPolicy.domain.policy.domain.entity.PolicyReply;
import seasonton.youthPolicy.domain.policy.domain.entity.PolicyReplyLike;
import seasonton.youthPolicy.domain.policy.domain.enums.EarnConditionCode;
import seasonton.youthPolicy.domain.policy.domain.enums.JobCode;
import seasonton.youthPolicy.domain.policy.domain.enums.PolicyStatus;
import seasonton.youthPolicy.domain.policy.domain.enums.SchoolCode;
import seasonton.youthPolicy.domain.policy.domain.repository.PolicyLikeRepository;
import seasonton.youthPolicy.domain.policy.domain.repository.PolicyReplyLikeRepository;
import seasonton.youthPolicy.domain.policy.domain.repository.PolicyReplyRepository;
import seasonton.youthPolicy.domain.policy.dto.PolicyRequestDTO;
import seasonton.youthPolicy.domain.policy.dto.PolicyResponseDTO;
import seasonton.youthPolicy.domain.policy.exception.PolicyException;

import seasonton.youthPolicy.domain.post.converter.PostConverter;
import seasonton.youthPolicy.domain.report.dto.perplexityDTO;
import seasonton.youthPolicy.global.common.RegionCodeMapper;
import seasonton.youthPolicy.global.error.code.status.ErrorStatus;
import seasonton.youthPolicy.global.infra.PerplexityClient;

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
    private final PolicyLikeRepository policyLikeRepository;
    private final PerplexityClient perplexityClient;
    private final PolicyReplyLikeRepository policyReplyLikeRepository;


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
    대분류:lclsfNm
    * */

    // 최신 순 조회
    public PolicyResponseDTO.PolicyListResponse getPolicies(int pageNum, int pageSize) {
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
            JsonNode resultNode = root.path("result");
            JsonNode items = resultNode.path("youthPolicyList");

            if (items.isMissingNode() || !items.isArray()) {
                throw new PolicyException(ErrorStatus.POLICY_NOT_FOUND);
            }

            // API에서 내려주는 전체 개수
            int totalCount = resultNode.path("paging").path("totalCount").asInt(-1);

            List<PolicyResponseDTO.YouthPolicyResponse> results = new ArrayList<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            for (JsonNode item : items) {
                Set<String> regionSet = new HashSet<>();
                String zipCodes = item.path("zipCd").asText(null);
                if (zipCodes != null && !zipCodes.isBlank()) {
                    for (String code : zipCodes.split(",")) {
                        String regionName = regionCodeMapper.getRegionName(code.trim());
                        if (regionName != null) {
                            regionSet.add(extractTopLevel(regionName));
                        }
                    }
                }

                String startDate = item.path("bizPrdBgngYmd").asText(null);
                String endDate = item.path("bizPrdEndYmd").asText(null);
                PolicyStatus status = calculateStatus(startDate, endDate);

                Long likeCount = policyLikeRepository.countByPlcyNo(item.path("plcyNo").asText(null));

                results.add(
                        PolicyResponseDTO.YouthPolicyResponse.builder()
                                .plcyNo(item.path("plcyNo").asText(null))
                                .plcyNm(item.path("plcyNm").asText(null))
                                .regionNames(regionSet)
                                .frstRegDt(item.path("frstRegDt").asText(null))
                                .lclsfNm(item.path("lclsfNm").asText(null))
                                .likeCount(likeCount)
                                .status(status)
                                .startDate(startDate == null || startDate.isBlank() ? "상시" : startDate)
                                .endDate(endDate == null || endDate.isBlank() ? "상시" : endDate)
                                .bizPrdBgngYmd(item.path("bizPrdBgngYmd").asText(null))
                                .bizPrdEndYmd(item.path("bizPrdEndYmd").asText(null))
                                .build()
                );
            }

            if (totalCount == -1) {
                totalCount = results.size();
            }

            results.sort(
                    Comparator.comparing(
                                    (PolicyResponseDTO.YouthPolicyResponse p) ->
                                            LocalDateTime.parse(p.getFrstRegDt(), formatter))
                            .reversed()
                            .thenComparing(PolicyResponseDTO.YouthPolicyResponse::getPlcyNm,
                                    Comparator.nullsLast(String::compareTo))
            );

            // 전체 개수 + 정책 리스트 반환
            return PolicyResponseDTO.PolicyListResponse.builder()
                    .totalCount(totalCount)
                    .policies(results)
                    .build();

        } catch (Exception e) {
            throw new PolicyException(ErrorStatus.POLICY_API_ERROR);
        }
    }

    // 좋아요 순 조회
    public PolicyResponseDTO.PolicyLikeListResponse getPoliciesOrderByLikes(int pageNum, int pageSize) {
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
            JsonNode resultNode = root.path("result");
            JsonNode items = resultNode.path("youthPolicyList");

            if (items.isMissingNode() || !items.isArray()) {
                throw new PolicyException(ErrorStatus.POLICY_NOT_FOUND);
            }

            int totalCount = resultNode.path("paging").path("totalCount").asInt(-1);

            List<PolicyResponseDTO.YouthPolicyLikeResponse> results = new ArrayList<>();

            for (JsonNode item : items) {
                Set<String> regionSet = new HashSet<>();
                String zipCodes = item.path("zipCd").asText(null);
                if (zipCodes != null && !zipCodes.isBlank()) {
                    for (String code : zipCodes.split(",")) {
                        String regionName = regionCodeMapper.getRegionName(code.trim());
                        if (regionName != null) {
                            regionSet.add(extractTopLevel(regionName));
                        }
                    }
                }

                String startDate = item.path("bizPrdBgngYmd").asText(null);
                String endDate = item.path("bizPrdEndYmd").asText(null);
                PolicyStatus status = calculateStatus(startDate, endDate);

                results.add(
                        PolicyResponseDTO.YouthPolicyLikeResponse.builder()
                                .plcyNo(item.path("plcyNo").asText(null))
                                .plcyNm(item.path("plcyNm").asText(null))
                                .regionNames(regionSet)
                                .frstRegDt(item.path("frstRegDt").asText(null))
                                .lclsfNm(item.path("lclsfNm").asText(null))
                                .likeCount(policyLikeRepository.countByPlcyNo(item.path("plcyNo").asText(null)))
                                .status(status)
                                .startDate(startDate == null || startDate.isBlank() ? "상시" : startDate)
                                .endDate(endDate == null || endDate.isBlank() ? "상시" : endDate)
                                .bizPrdBgngYmd(item.path("bizPrdBgngYmd").asText(null))
                                .bizPrdEndYmd(item.path("bizPrdEndYmd").asText(null))
                                .build()
                );
            }

            if (totalCount == -1) {
                totalCount = results.size();
            }

            // 정렬
            results.sort(
                    Comparator.comparing(PolicyResponseDTO.YouthPolicyLikeResponse::getLikeCount).reversed()
                            .thenComparing(PolicyResponseDTO.YouthPolicyLikeResponse::getPlcyNm,
                                    Comparator.nullsLast(String::compareTo))
            );

            return PolicyResponseDTO.PolicyLikeListResponse.builder()
                    .totalCount(totalCount)
                    .policies(results)
                    .build();

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
                    + "&plcyNo=" + plcyNo;

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

            // 지역 변환 (시/도 단위만)
            Set<String> regionSet = new HashSet<>();
            String zipCodes = item.path("zipCd").asText(null);
            if (zipCodes != null && !zipCodes.isBlank()) {
                String[] codes = zipCodes.split(",");
                for (String code : codes) {
                    String regionName = regionCodeMapper.getRegionName(code.trim());
                    if (regionName != null) {
                        String topLevelRegion = extractTopLevel(regionName); // 시/도 단위 추출
                        regionSet.add(topLevelRegion);
                    }
                }
            }


            return PolicyResponseDTO.YouthPolicyDetailResponse.builder()
                    .plcyNo(item.path("plcyNo").asText(null))
                    .plcyNm(item.path("plcyNm").asText(null))
                    .regions(regionSet)
                    .aplyUrlAddr(item.path("aplyUrlAddr").asText(null))
                    .plcyExplnCn(item.path("plcyExplnCn").asText(null))
                    .plcySprtCn(item.path("plcySprtCn").asText(null))
                    .sprtSclLmtYn(item.path("sprtSclLmtYn").asText(null))
                    .plcyAplyMthdCn(item.path("plcyAplyMthdCn").asText(null))
                    .sprtTrgtMinAge(item.hasNonNull("sprtTrgtMinAge") ? item.get("sprtTrgtMinAge").asInt() : null)
                    .sprtTrgtMaxAge(item.hasNonNull("sprtTrgtMaxAge") ? item.get("sprtTrgtMaxAge").asInt() : null)
                    .sprtTrgtAgeLmtYn(item.path("sprtTrgtAgeLmtYn").asText(null))
                    .schoolCd(SchoolCode.fromCode(item.path("schoolCd").asText(null)))
                    .jobCd(JobCode.fromCode(item.path("jobCd").asText(null)))
                    .earnCndSeCd(EarnConditionCode.fromCode(item.path("earnCndSeCd").asText(null)))
                    .earnMinAmt(item.hasNonNull("earnMinAmt") ? item.get("earnMinAmt").asInt() : null)
                    .earnMaxAmt(item.hasNonNull("earnMaxAmt") ? item.get("earnMaxAmt").asInt() : null)
                    .earnEtcCn(item.path("earnEtcCn").asText(null))
                    .addAplyQlfcCndCn(item.path("addAplyQlfcCndCn").asText(null))
                    .sbmsnDcmntCn(item.path("sbmsnDcmntCn").asText(null))
                    .srngMthdCn(item.path("srngMthdCn").asText(null))
                    .frstRegDt(item.path("frstRegDt").asText(null))
                    .lastMdfcnDt(item.path("lastMdfcnDt").asText(null))
                    .lclsfNm(item.path("lclsfNm").asText(null))
                    .startDate(item.path("bizPrdBgngYmd").asText(null))
                    .endDate(item.path("bizPrdEndYmd").asText(null))
                    .aplyYmd(item.path("aplyYmd").asText(null))
                    .bizPrdBgngYmd(item.path("bizPrdBgngYmd").asText(null))
                    .bizPrdEndYmd(item.path("bizPrdEndYmd").asText(null))
                    .build();

        } catch (PolicyException e) {
            throw e;
        } catch (Exception e) {
            throw new PolicyException(ErrorStatus.POLICY_API_ERROR);
        }
    }


    // 댓글 작성
    @Transactional
    public PolicyResponseDTO.ReplyCreateResponse createReply(Long userId, PolicyRequestDTO.Create request, boolean isAnonymous) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new PolicyException(ErrorStatus.USER_NOT_FIND));

        PolicyResponseDTO.ReplyFilterResponse filterResult = filterComment(request.getContent());
        if (!filterResult.isAllowed()) {
            throw new PolicyException(ErrorStatus.REPLY_FILTERED);
        }

        PolicyReply reply = PolicyReply.builder()
                .content(request.getContent())
                .isAnonymous(isAnonymous)
                .plcyNo(request.getPlcyNo())
                .plcyNm(request.getPlcyNm())
                .writer(isAnonymous ? "익명" : user.getNickname())
                .user(user)
                .build();

        policyReplyRepository.save(reply);

        return PolicyResponseDTO.ReplyCreateResponse.builder()
                .id(reply.getId())
                .content(reply.getContent())
                .isAnonymous(reply.isAnonymous())
                .plcyNo(reply.getPlcyNo())
                .plcyNm(reply.getPlcyNm())
                .writer(reply.getWriter())
                .build();
    }

    // 댓글 조회
    public List<PolicyResponseDTO.ReplyListResponse> getReplies(String plcyNo) {
        return policyReplyRepository.findByPlcyNo(plcyNo).stream()
                .map(reply -> {
                        Long likeCnt = policyReplyLikeRepository.countByPolicyReplyId(reply.getId());
                        return PolicyResponseDTO.ReplyListResponse.builder()
                        .id(reply.getId())
                        .content(reply.getContent())
                        .isAnonymous(reply.isAnonymous())
                        .plcyNo(reply.getPlcyNo())
                        .plcyNm(reply.getPlcyNm())
                        .writer(reply.isAnonymous() ? "익명" : reply.getUser().getNickname())
                        .likeCount(likeCnt)
                        .build();
                })
                .collect(Collectors.toList());
    }

    // 댓글 수정
    @Transactional
    public PolicyResponseDTO.ReplyUpdateResponse updateReply(Long replyId, Long userId, PolicyRequestDTO.ReplyUpdateRequest request, boolean isAnonymous) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new PolicyException(ErrorStatus.USER_NOT_FIND));

        PolicyReply reply = policyReplyRepository.findById(replyId)
                .orElseThrow(() -> new PolicyException(ErrorStatus.REPLY_NOT_FOUND));

        if (!reply.getUser().getId().equals(userId)) {
            throw new PolicyException(ErrorStatus.REPLY_FORBIDDEN);
        }

        // 엔티티에 업데이트용 메서드 추가해서 상태 변경
        reply.updateReply(request.getContent(), isAnonymous, isAnonymous ? "익명" : user.getNickname());

        return PolicyResponseDTO.ReplyUpdateResponse.builder()
                .id(reply.getId())
                .content(reply.getContent())
                .isAnonymous(reply.isAnonymous())
                .plcyNo(reply.getPlcyNo())
                .plcyNm(reply.getPlcyNm())
                .writer(reply.getWriter())
                .build();
    }

    // 댓글 삭제
    @Transactional
    public PolicyResponseDTO.ReplyDeleteResponse deleteReply(Long replyId, Long userId) {
        PolicyReply reply = policyReplyRepository.findById(replyId)
                .orElseThrow(() -> new PolicyException(ErrorStatus.REPLY_NOT_FOUND));

        if (!reply.getUser().getId().equals(userId)) {
            throw new PolicyException(ErrorStatus.REPLY_FORBIDDEN);
        }

        policyReplyRepository.delete(reply);

        return PolicyResponseDTO.ReplyDeleteResponse.builder()
                .id(reply.getId())
                .message("댓글이 성공적으로 삭제되었습니다.")
                .build();
    }

    // 정책 댓글 좋아요 추가/취소
    @Transactional
    public void policyReplyToggleLike(Long userId, Long replyId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new PolicyException(ErrorStatus.USER_NOT_FIND));

        PolicyReply reply = policyReplyRepository.findById(replyId)
                .orElseThrow(() -> new PolicyException(ErrorStatus.POLICY_REPLY_NOT_FOUND));

        // 이미 좋아요 한 경우 → 취소
        policyReplyLikeRepository.findByUserIdAndPolicyReplyId(userId, replyId)
                .ifPresentOrElse(
                        policyReplyLikeRepository::delete, () -> {
                            // 좋아요 추가
                            PolicyReplyLike newLike = PolicyReplyLike.builder()
                                    .user(user)
                                    .policyReply(reply)
                                    .build();
                            policyReplyLikeRepository.save(newLike);
                        }
                );
    }

    // 정책 댓글 좋아요 개수 조회
    public Long getPolicyReplyLikeCount(Long replyId) {
        return policyReplyLikeRepository.countByPolicyReplyId(replyId);
    }
    // 댓글 요약
    public PolicyResponseDTO.ReplySummaryResponse summarizeReplies(String plcyNo, String plcyNm) {
        List<PolicyReply> replies = policyReplyRepository.findByPlcyNo(plcyNo);

        if (replies.isEmpty()) {
            return PolicyResponseDTO.ReplySummaryResponse.builder()
                    .plcyNo(plcyNo)
                    .summary("댓글이 아직 존재하지 않습니다.")
                    .build();
        }

        String combined = replies.stream()
                .map(PolicyReply::getContent)
                .collect(Collectors.joining("\n"));

        perplexityDTO.PerplexityChatResponse resp = perplexityClient.summarize(
                "다음은 정책 '" + plcyNm + "'에 대한 댓글들입니다. 핵심 의견을 요약해줘:\n" + combined
        );

        String summary = resp.getChoices()[0].getMessage().getContent();

        return PolicyResponseDTO.ReplySummaryResponse.builder()
                .plcyNo(plcyNo)
                .summary(summary)
                .build();
    }

    // 댓글 자동 필터링 -> 2차로 검사
    @Transactional
    public List<Long> autoDeleteAbnormalReplies(String plcyNo) {
        List<PolicyReply> replies = policyReplyRepository.findByPlcyNo(plcyNo);

        List<Long> deletedIds = new ArrayList<>();

        for (PolicyReply reply : replies) {
            PolicyResponseDTO.ReplyFilterResponse filter = filterComment(reply.getContent());

            if (!filter.isAllowed()) {
                policyReplyRepository.delete(reply);
                deletedIds.add(reply.getId());
            }
        }

        return deletedIds;
    }


    // 정책 좋아요 토글 (추가/취소)
    @Transactional
    public String toggleLike(Long userId, String plcyNo) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new PolicyException(ErrorStatus.USER_NOT_FIND));

        PolicyLike existing = policyLikeRepository.findByUserAndPlcyNo(user, plcyNo).orElse(null);

        if (existing != null) {
            policyLikeRepository.delete(existing);
            return "좋아요 취소됨";
        } else {
            PolicyLike like = PolicyLike.builder()
                    .user(user)
                    .plcyNo(plcyNo)
                    .build();
            policyLikeRepository.save(like);
            return "좋아요 추가됨";
        }
    }

    // 정책 좋아요 개수 조회
    public long getLikeCount(String plcyNo) {
        return policyLikeRepository.countByPlcyNo(plcyNo);
    }

    // 정책 검색
    public PolicyResponseDTO.PolicySearchListResponse searchPolicies(
            List<String> categories, String plcyNm, List<String> regions,
            int pageNum, int pageSize) {

        try {
            Set<String> seen = new HashSet<>();
            List<PolicyResponseDTO.YouthPolicySearchResponse> results = new ArrayList<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            // 카테고리가 없으면 전체 조회도 가능하도록 처리
            if (categories == null || categories.isEmpty()) {
                categories = List.of(""); // 빈 값으로 전체 조회
            }

            for (String category : categories) {
                String url = baseUrl
                        + "?apiKeyNm=" + apiKey
                        + "&rtnType=json"
                        + "&pageNum=" + pageNum
                        + "&pageSize=" + pageSize;

                if (category != null && !category.isBlank()) {
                    url += "&lclsfNm=" + category;
                }

                // 정책명 검색 (상세보기랑 동일하게 그대로 붙임)
                if (plcyNm != null && !plcyNm.isBlank()) {
                    url += "&plcyNm=" + plcyNm;
                }

                String response = restTemplate.getForObject(url, String.class);
                if (response == null || response.isBlank()) continue;

                JsonNode root = objectMapper.readTree(response);
                JsonNode items = root.path("result").path("youthPolicyList");
                if (items.isMissingNode() || !items.isArray()) continue;

                for (JsonNode item : items) {
                    String plcyNo = item.path("plcyNo").asText(null);
                    if (plcyNo != null && seen.add(plcyNo)) {

                        // 지역 처리
                        Set<String> regionSet = new HashSet<>();
                        String zipCodes = item.path("zipCd").asText(null);
                        if (zipCodes != null && !zipCodes.isBlank()) {
                            for (String code : zipCodes.split(",")) {
                                String regionName = regionCodeMapper.getRegionName(code.trim());
                                if (regionName != null) {
                                    regionSet.add(extractTopLevel(regionName));
                                }
                            }
                        }

                        // 지역 태그 필터링
                        if (regions != null && !regions.isEmpty()) {
                            boolean matched = regionSet.stream()
                                    .anyMatch(region -> regions.stream().anyMatch(region::contains));
                            if (!matched) continue;
                        }


                        // 좋아요 수
                        Long likeCount = policyLikeRepository.countByPlcyNo(plcyNo);

                        // 정책 상태 계산
                        String startDate = item.path("bizPrdBgngYmd").asText(null);
                        String endDate = item.path("bizPrdEndYmd").asText(null);
                        PolicyStatus status = calculateStatus(startDate, endDate);

                        results.add(
                                PolicyResponseDTO.YouthPolicySearchResponse.builder()
                                        .plcyNo(plcyNo)
                                        .plcyNm(item.path("plcyNm").asText(null))
                                        .regionNames(regionSet)
                                        .frstRegDt(item.path("frstRegDt").asText(null))
                                        .lclsfNm(item.path("lclsfNm").asText(null))
                                        .likeCount(likeCount)
                                        .status(status)
                                        .startDate(startDate == null || startDate.isBlank() ? "상시" : startDate)
                                        .endDate(endDate == null || endDate.isBlank() ? "상시" : endDate)
                                        .bizPrdBgngYmd(item.path("bizPrdBgngYmd").asText(null))
                                        .bizPrdEndYmd(item.path("bizPrdEndYmd").asText(null))
                                        .build()
                        );
                    }
                }
            }

            if (results.isEmpty()) {
                throw new PolicyException(ErrorStatus.POLICY_NOT_FOUND);
            }

            // 최신순 정렬
            results.sort(
                    Comparator.comparing(
                                    (PolicyResponseDTO.YouthPolicySearchResponse p) ->
                                            LocalDateTime.parse(p.getFrstRegDt(), formatter))
                            .reversed()
                            .thenComparing(PolicyResponseDTO.YouthPolicySearchResponse::getPlcyNm,
                                    Comparator.nullsLast(String::compareTo))
            );

            return PolicyResponseDTO.PolicySearchListResponse.builder()
                    .totalCount(results.size())
                    .policies(results)
                    .build();

        } catch (PolicyException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            throw new PolicyException(ErrorStatus.POLICY_INVALID_REQUEST);
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

    // 댓글 필터링
    private PolicyResponseDTO.ReplyFilterResponse filterComment(String content) {
        String prompt = "다음 댓글이 욕설/비방/스팸/도배라면 'BLOCK: 이유'로, 정상적이면 'OK'라고만 답해:\n" + content;

        perplexityDTO.PerplexityChatResponse resp = perplexityClient.summarize(prompt);
        String answer = resp.getChoices()[0].getMessage().getContent().trim();

        if (answer.startsWith("OK")) {
            return PolicyResponseDTO.ReplyFilterResponse.builder()
                    .allowed(true)
                    .reason("정상 댓글")
                    .build();
        } else {
            return PolicyResponseDTO.ReplyFilterResponse.builder()
                    .allowed(false)
                    .reason(answer.replace("BLOCK:", "").trim())
                    .build();
        }
    }
}