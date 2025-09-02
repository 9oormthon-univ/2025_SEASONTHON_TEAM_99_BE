package seasonton.youthPolicy.domain.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import seasonton.youthPolicy.domain.member.domain.entity.User;
import seasonton.youthPolicy.domain.member.dto.MemberCreateDto;
import seasonton.youthPolicy.domain.member.dto.MemberLoginDto;
import seasonton.youthPolicy.domain.member.service.MemberService;
import seasonton.youthPolicy.global.auth.JwtTokenProvider;
import seasonton.youthPolicy.global.dto.TokenDTO;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "청정 자체 계정 회원가입")
    public ResponseEntity<User> signup(@RequestBody MemberCreateDto memberCreateDto){
        User user = memberService.create(memberCreateDto);
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    @PostMapping("/doLogin")
    @Operation(summary = "로그인", description = "청정 자체 계정 회원가입")
    public ResponseEntity<TokenDTO> doLogin(@RequestBody MemberLoginDto memberLoginDto) {
        User user = memberService.login(memberLoginDto);
        TokenDTO jwtToken = jwtTokenProvider.createToken(user.getId(), user.getEmail(), user.getAuthority().toString());

        return new ResponseEntity<>(jwtToken, HttpStatus.OK);
    }

}
