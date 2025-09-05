package seasonton.youthPolicy.domain.report.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

public class ReportResponseDTO {

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReportDetailResponse {
        private int year;
        private int month;
        private Long regionId;
        @Size(max = 50)
        private String regionName;
        private String content;
        private LocalDateTime createdAt;
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReportListResponse {
        private Long reportId;
        private Long regionId;
        @Size(max = 50)
        private String regionName;
    }

}
