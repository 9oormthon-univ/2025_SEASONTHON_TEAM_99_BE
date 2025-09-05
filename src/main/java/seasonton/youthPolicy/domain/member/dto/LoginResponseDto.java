package seasonton.youthPolicy.domain.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import seasonton.youthPolicy.global.dto.TokenDTO;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class LoginResponseDto {
    String email;
    String nickname;
    Long regionId;
    String regionName;
    String profileImageUrl;
    TokenDTO token;
}
