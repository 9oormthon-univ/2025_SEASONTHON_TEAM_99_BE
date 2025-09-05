package seasonton.youthPolicy.domain.report.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.Year;
import java.time.YearMonth;

public class ReportRequestDTO {

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReportCreateDTO{

        private YearMonth yearmonth;
        private Long regionId;

    }

}
