package zaman.dongnaemoa.domain.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import team.themoment.sdk.response.CommonApiResponse;
import zaman.dongnaemoa.domain.user.dto.LoginRequest;
import zaman.dongnaemoa.domain.user.dto.LoginResponse;
import zaman.dongnaemoa.domain.user.dto.SignUpRequest;
import zaman.dongnaemoa.domain.user.dto.SignUpResponse;
import zaman.dongnaemoa.domain.user.service.AuthService;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public CommonApiResponse<SignUpResponse> signUp(@Valid @RequestBody SignUpRequest request) {
        SignUpResponse response = authService.signUp(request);
        return CommonApiResponse.created("회원가입이 완료되었습니다.", response);
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
