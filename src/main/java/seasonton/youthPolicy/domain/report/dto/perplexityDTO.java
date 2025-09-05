package seasonton.youthPolicy.domain.report.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

public class perplexityDTO {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PerplexityChatRequest {
        private String model;
        private PerplexityMessage[] messages;

        // 선택 파라미터들
        private Boolean disable_search;
        private Boolean return_images;
        private Boolean return_related_questions;
        private Double presence_penalty;
        private Double frequency_penalty;
        private Boolean return_citations;
        private String[] search_domain_filter;
        private Double top_p;
        private Double temperature;
        private Integer max_tokens;
        private Boolean stream;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PerplexityChatResponse {
        private PerplexityChoice[] choices;

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        public static class PerplexityChoice {
            private PerplexityMessage message;
        }
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class PerplexityMessage {
        private String role;    // "system" | "user" | "assistant"
        private String content;
    }
}
