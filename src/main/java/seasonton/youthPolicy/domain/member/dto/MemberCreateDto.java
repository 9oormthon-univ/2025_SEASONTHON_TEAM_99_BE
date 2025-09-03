package seasonton.youthPolicy.domain.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@AllArgsConstructor
public class MemberCreateDto {
    String email;
    String password;
    String nickname;
    String region;
    MultipartFile profile;
}
