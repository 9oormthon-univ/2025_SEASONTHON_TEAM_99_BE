package seasonton.youthPolicy.global.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class S3DTO {

    @Getter
    @AllArgsConstructor
    public static class UploadResult {
        private String originalName;
        private String storedName;
        private String url;
    }
}
