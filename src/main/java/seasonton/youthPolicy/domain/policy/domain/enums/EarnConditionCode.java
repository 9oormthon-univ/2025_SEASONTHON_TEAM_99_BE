package seasonton.youthPolicy.domain.policy.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EarnConditionCode {

    무관("0043001", "무관"),
    연소득("0043002", "연소득"),
    기타("0043003", "기타");

    private final String code;
    private final String name;

    @JsonValue
    public String getName() {
        return name;
    }

    @JsonCreator
    public static EarnConditionCode fromCode(String code) {
        for (EarnConditionCode e : values()) {
            if (e.code.equals(code)) return e;
        }
        return null;
    }
}
