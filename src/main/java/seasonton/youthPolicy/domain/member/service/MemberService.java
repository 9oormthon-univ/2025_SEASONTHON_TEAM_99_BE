package seasonton.youthPolicy.domain.member.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seasonton.youthPolicy.domain.member.domain.entity.User;
import seasonton.youthPolicy.domain.member.domain.repository.UserRepository;
import seasonton.youthPolicy.domain.member.dto.MemberCreateDto;
import seasonton.youthPolicy.domain.member.dto.MemberLoginDto;
import seasonton.youthPolicy.global.auth.JwtTokenProvider;
import seasonton.youthPolicy.global.dto.TokenDTO;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtTokenProvider jwtTokenProvider;

    public User create(MemberCreateDto memberCreateDto) {
        if (userRepository.existsByEmail(memberCreateDto.getEmail())){
            throw new RuntimeException("User already registered!");
        }

        User user = User.builder()
                .email(memberCreateDto.getEmail())
                .password(passwordEncoder.encode(memberCreateDto.getPassword()))
                .nickname(memberCreateDto.getNickname())
                .region(null) // 나중에 회원가입시 지역구 설정 시 바꿔야하는 부분
                .build();
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
