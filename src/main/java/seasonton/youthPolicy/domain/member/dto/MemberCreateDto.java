package seasonton.youthPolicy.domain.member.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberCreateDto {
    String email;
    String password;
    String nickname;
    String region;
}
