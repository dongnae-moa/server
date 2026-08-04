package zaman.dongnaemoa.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import team.themoment.sdk.response.CommonApiResponse;
import zaman.dongnaemoa.domain.user.dto.UserResponse;
import zaman.dongnaemoa.domain.user.service.UserService;
import zaman.dongnaemoa.global.security.CustomUserDetails;

@Tag(name = "User", description = "내 정보 조회 API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "내 정보 조회",
            description = "로그인한 사용자 본인의 이메일/닉네임/보유 포인트/가입한 동네 정보를 조회한다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(
                    examples = @ExampleObject(value = """
                            {
                              "status": "OK",
                              "code": 200,
                              "message": "조회에 성공했습니다.",
                              "data": {
                                "id": 1,
                                "email": "user@example.com",
                                "nickname": "동네주민",
                                "point": 150,
                                "neighborhood": {"name": "청운동", "sido": "서울특별시", "sigungu": "종로구"}
                              }
                            }
                            """))),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @GetMapping("/me")
    public CommonApiResponse<UserResponse> me(@AuthenticationPrincipal CustomUserDetails principal) {
        return CommonApiResponse.success("조회에 성공했습니다.", userService.getMe(principal.getUserId()));
    }
}
