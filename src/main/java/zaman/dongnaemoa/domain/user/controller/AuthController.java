package zaman.dongnaemoa.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Auth", description = "회원가입 / 로그인 API. 인증이 필요 없는 유일한 엔드포인트들이며, 로그인 성공 시 발급되는 accessToken을 이후 모든 요청의 Authorization 헤더에 담아야 함")
@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "회원가입",
            description = "이메일/비밀번호/닉네임으로 신규 계정을 생성한다. 이메일과 닉네임은 각각 중복될 수 없다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "회원가입 성공"),
            @ApiResponse(responseCode = "400", description = "요청 값 검증 실패 (이메일 형식, 비밀번호/닉네임 길이 등)"),
            @ApiResponse(responseCode = "409", description = "이미 가입된 이메일 또는 이미 사용 중인 닉네임")
    })
    @PostMapping("/signup")
    public CommonApiResponse<SignUpResponse> signUp(@Valid @RequestBody SignUpRequest request) {
        SignUpResponse response = authService.signUp(request);
        return CommonApiResponse.created("회원가입이 완료되었습니다.", response);
    }

    @Operation(
            summary = "로그인",
            description = "이메일/비밀번호로 로그인하고 JWT 액세스 토큰을 발급받는다. "
                    + "보안을 위해 이메일이 존재하지 않는 경우와 비밀번호가 틀린 경우 모두 동일한 401 메시지를 반환한다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공, accessToken 발급"),
            @ApiResponse(responseCode = "401", description = "이메일 또는 비밀번호가 올바르지 않음")
    })
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
