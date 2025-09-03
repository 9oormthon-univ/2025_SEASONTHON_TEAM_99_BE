package seasonton.youthPolicy.domain.policy.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PolicyStatus {
    NOT_STARTED("진행전"),
    IN_PROGRESS("진행중"),
    COMPLETED("완료");

    private final String description;

    // 응답 시 description으로 변환되도록
    @JsonValue
    public String getDescription() {
        return description;
    }
}
