package seasonton.youthPolicy.domain.policy.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum JobCode {

    재직자("0013001", "재직자"),
    자영업자("0013002", "자영업자"),
    미취업자("0013003", "미취업자"),
    프리랜서("0013004", "프리랜서"),
    일용근로자("0013005", "일용근로자"),
    예비창업자("0013006", "(예비)창업자"),
    단기근로자("0013007", "단기근로자"),
    영농종사자("0013008", "영농종사자"),
    기타("0013009", "기타"),
    제한없음("0013010", "제한없음");

    private final String code;
    private final String name;

    @JsonValue
    public String getName() {
        return name;
    }

    @JsonCreator
    public static JobCode fromCode(String code) {
        for (JobCode j : values()) {
            if (j.code.equals(code)) return j;
        }
        return null;
    }
}
