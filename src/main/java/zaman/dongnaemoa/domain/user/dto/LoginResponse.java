package zaman.dongnaemoa.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import zaman.dongnaemoa.domain.user.entity.User;

@Schema(description = "로그인 응답")
public record LoginResponse(
        @Schema(description = "JWT 액세스 토큰. Authorization 헤더에 'Bearer {accessToken}' 형태로 담아 사용",
                example = "eyJhbGciOiJIUzI1NiJ9...") String accessToken,
        @Schema(description = "토큰 타입", example = "Bearer") String tokenType,
        @Schema(description = "사용자 ID", example = "1") Long userId,
        @Schema(description = "닉네임", example = "동네주민") String nickname
) {
    public static LoginResponse of(String accessToken, User user) {
        return new LoginResponse(accessToken, "Bearer", user.getId(), user.getNickname());
    }
}
