package seasonton.youthPolicy.global.dto;

import lombok.*;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TokenDTO {
    String grantType;
    String accessToken;
    Date accessTokenExpiresIn;
}