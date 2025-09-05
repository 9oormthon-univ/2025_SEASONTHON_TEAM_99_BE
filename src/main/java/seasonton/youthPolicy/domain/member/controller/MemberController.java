package seasonton.youthPolicy.domain.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import seasonton.youthPolicy.domain.member.domain.entity.User;
import seasonton.youthPolicy.domain.member.dto.CheckDto;
import seasonton.youthPolicy.domain.member.dto.LoginResponseDto;
import seasonton.youthPolicy.domain.member.dto.MemberCreateDto;
import seasonton.youthPolicy.domain.member.dto.MemberLoginDto;
import seasonton.youthPolicy.domain.member.service.MemberService;
import seasonton.youthPolicy.global.auth.JwtTokenProvider;
import seasonton.youthPolicy.global.dto.TokenDTO;

import java.util.Map;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping(value = "/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "회원가입", description = "청정 자체 계정 회원가입 (regionId에는 매핑 전 id번호를 넣어주세요)")
    public ResponseEntity<User> signup(
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String nickname,
            @RequestParam Long regionId,
            @RequestPart("imageFile") MultipartFile imageFile
    ){
        MemberCreateDto memberCreateDto = new MemberCreateDto(email, password, nickname, regionId, imageFile);
        User user = memberService.create(memberCreateDto);
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    @PostMapping("/doLogin")
    @Operation(summary = "로그인", description = "청정 자체 계정 회원가입")
    public ResponseEntity<LoginResponseDto> doLogin(@RequestBody MemberLoginDto memberLoginDto) {
        User user = memberService.login(memberLoginDto);
        TokenDTO jwtToken = jwtTokenProvider.createToken(user.getId(), user.getEmail(), user.getAuthority().toString());

        LoginResponseDto result = LoginResponseDto.builder()
                .profileImageUrl(user.getProfileImageUrl())
                .token(jwtToken)
                .regionId(user.getRegion() == null ? null : user.getRegion().getId())
                .regionName(user.getRegion() == null ? null : user.getRegion().getRegionName())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .build();

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/emailCheck")
    @Operation(summary = "이메일 중복체크", description = "email만 스트링으로 보내주시면 됩니다")
    public ResponseEntity<Map<String, String>> emailCheck(@RequestBody CheckDto checkDto) {
        String available = memberService.emailCheck(checkDto);
        return ResponseEntity.ok(Map.of("available", available));
    }

    @GetMapping("/nameCheck")
    @Operation(summary = "닉네임 중복체크", description = "닉네임만 스트링으로 보내주시면 됩니다")
    public ResponseEntity<Map<String, String>> nameCheck(@RequestBody CheckDto checkDto) {
        String available = memberService.nameCheck(checkDto);
        return ResponseEntity.ok(Map.of("available", available));
    }
}
