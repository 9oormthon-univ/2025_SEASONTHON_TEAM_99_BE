package seasonton.youthPolicy.domain.policy.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SchoolCode {
    고졸미만("0049001", "고졸 미만"),
    고교재학("0049002", "고교 재학"),
    고교예정("0049003", "고교 예정"),
    고교졸업("0049004", "고교 졸업"),
    대학재학("0049005", "대학 재학"),
    대학예정("0049006", "대학 예정"),
    대학졸업("0049007", "대학 졸업"),
    석박사("0049008", "석·박사"),
    기타("0049009", "기타"),
    제한없음("0049010", "제한없음");

    private final String code;
    private final String name;

    @JsonValue   // 직렬화 시 → name 값 반환
    public String getName() {
        return name;
    }

    @JsonCreator // 역직렬화 시 → code 기준 매핑
    public static SchoolCode fromCode(String code) {
        for (SchoolCode s : values()) {
            if (s.code.equals(code)) return s;
        }
        return null;
    }
}
