package seasonton.youthPolicy.domain.member.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import seasonton.youthPolicy.domain.member.domain.entity.User;
import seasonton.youthPolicy.domain.member.domain.repository.UserRepository;
import seasonton.youthPolicy.domain.member.dto.MemberCreateDto;
import seasonton.youthPolicy.domain.member.dto.MemberLoginDto;
import seasonton.youthPolicy.global.auth.JwtTokenProvider;
import seasonton.youthPolicy.global.dto.S3DTO;
import seasonton.youthPolicy.global.dto.TokenDTO;
import seasonton.youthPolicy.global.service.S3Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtTokenProvider jwtTokenProvider;
    private final S3Service s3Service;

    @Value("${minio.dir.user-image}")
    private String userDir;

    public User create(MemberCreateDto memberCreateDto) {
        if (userRepository.existsByEmail(memberCreateDto.getEmail())){
            throw new RuntimeException("User already registered!");
        }

        User.UserBuilder builder = User.builder()
                .email(memberCreateDto.getEmail())
                .password(passwordEncoder.encode(memberCreateDto.getPassword()))
                .nickname(memberCreateDto.getNickname())
                .region(null); // 나중에 회원가입시 지역구 설정 시 바꿔야하는 부분

        MultipartFile profile = memberCreateDto.getProfile();

        if (profile != null && !profile.isEmpty()) {
            // MinIO/S3에 업로드 후 반환된 url 사용
            S3DTO.UploadResult result = s3Service.uploadFile(userDir, profile);
            String uploadedUrl = result.getUrl();
            builder.profileImageUrl(uploadedUrl);
        }

        User user = builder.build();
        userRepository.save(user);

        return user;
    }

    public User login(MemberLoginDto memberLoginDto) {
        Optional<User> optMember = userRepository.findByEmail(memberLoginDto.getEmail());
        if (!optMember.isPresent()) {
            throw new IllegalArgumentException("no email found");
        }

        User user = optMember.get();

        if (!passwordEncoder.matches(memberLoginDto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("wrong password");
        }

        return user;
    }


}
