package seasonton.youthPolicy.domain.report.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seasonton.youthPolicy.domain.model.entity.Region;
import seasonton.youthPolicy.domain.model.repository.RegionRepository;
import seasonton.youthPolicy.domain.post.converter.PostConverter;
import seasonton.youthPolicy.domain.post.domain.entity.Posts;
import seasonton.youthPolicy.domain.post.domain.repository.PostRepository;
import seasonton.youthPolicy.domain.post.dto.PostResponseDTO;
import seasonton.youthPolicy.domain.post.exception.PostException;
import seasonton.youthPolicy.domain.report.domain.entity.Report;
import seasonton.youthPolicy.domain.report.domain.repository.ReportRepository;
import seasonton.youthPolicy.domain.report.dto.ReportRequestDTO;
import seasonton.youthPolicy.domain.report.dto.ReportResponseDTO;
import seasonton.youthPolicy.domain.report.exception.ReportException;
import seasonton.youthPolicy.global.error.code.status.ErrorStatus;
import seasonton.youthPolicy.global.infra.PerplexityClient;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final PostRepository postRepository;
    private final PerplexityClient perplexityClient;
    private final RegionRepository regionRepository;
    private final ReportRepository reportRepository;

    @Transactional
    public ReportResponseDTO.ReportDetailResponse summarizePosts(ReportRequestDTO.ReportCreateDTO reportCreate){

        YearMonth ym = reportCreate.getYearmonth();
        int year = ym.getYear();
        int month = ym.getMonthValue();
        Long regionId = reportCreate.getRegionId();
        Region region = regionRepository.getReferenceById(regionId);

        if (reportRepository.findByYearmonthAndRegion(ym, region) != null) {
            throw new ReportException(ErrorStatus.REPORT_ALREADY_EXIST);
        }

        List<Posts> postTop5 = postRepository.findTopByYearMonthAndRegionOrderByLikeDesc(
                year, month, regionId, PageRequest.of(0, 5));

        int i = 0;
        StringBuilder sb = new StringBuilder();
        sb.append("아래는 5개의 게시글입니다. 각 게시글은 제목과 본문으로 구성됩니다.\n---\n");
        for (Posts eachPost : postTop5) {
            i++;
            String title = eachPost.getTitle();
            String content = eachPost.getContent();
            sb.append("[Post ").append(i).append("]\n");
            sb.append("Title: ").append(title).append("\n");
            sb.append("Body: ").append(content).append("\n");
            sb.append("---\n");
        }
        sb.append("요구사항: 위 게시글만 근거로 여론을 요약하고, 개선방향을 제시하세요. 출처 표기나 외부 검색은 금지합니다.");

        // 요약 생성
        var finalRes = perplexityClient.summarize(sb.toString(), 1);
        String finalSummary = finalRes.getChoices()[0].getMessage().getContent();

        Report generatedReport = Report.builder()
                .content(finalSummary)
                .yearmonth(ym)
                .region(region)
                .build();

        reportRepository.save(generatedReport);

        return ReportResponseDTO.ReportDetailResponse.builder()
                .year(year).month(month).createdAt(generatedReport.getCreatedAt())
                .regionId(regionId).regionName(region.getRegionName())
                .content(finalSummary).build();
    }

    // 글 상세 조회
    public ReportResponseDTO.ReportDetailResponse getDetailReport(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new PostException(ErrorStatus.REPORT_NOT_FOUND));

        return ReportResponseDTO.ReportDetailResponse.builder()
                .month(report.getYearmonth().getMonthValue())
                .year(report.getYearmonth().getYear())
                .regionName(report.getRegion().getRegionName())
                .regionId(report.getRegion().getId())
                .createdAt(report.getCreatedAt())
                .content(report.getContent())
                .build();
    }

    // 모든 글 조회, 월별로 분류
    public Map<String, List<ReportResponseDTO.ReportListResponse>> getListReport(YearMonth from, YearMonth to, Long regionId) {
        final DateTimeFormatter F = DateTimeFormatter.ofPattern("yyyy-MM");
        List<Report> reports = reportRepository.findAll(Sort.by(Sort.Direction.DESC, "yearmonth"));
        return reports.stream()
                .filter(r -> from == null || !r.getYearmonth().isBefore(from))
                .filter(r -> to   == null || !r.getYearmonth().isAfter(to))
                .filter(r -> regionId == null || r.getRegion().getId().equals(regionId))
                .collect(Collectors.groupingBy(
                        r -> r.getYearmonth().format(F),     // Map의 키: "yyyy-MM"
                        LinkedHashMap::new,                         // 순서 보존
                        Collectors.mapping(this::toEachSimple,      // 값: DTO 리스트
                                Collectors.toList())
                ));
    }

    private ReportResponseDTO.ReportListResponse toEachSimple(Report r) {
        return ReportResponseDTO.ReportListResponse.builder()
                .reportId(r.getId())
                .regionId(r.getRegion().getId())
                .regionName(r.getRegion().getRegionName())
                .build();
    }
}
