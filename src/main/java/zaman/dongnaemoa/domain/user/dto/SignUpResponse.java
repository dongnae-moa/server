package zaman.dongnaemoa.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import zaman.dongnaemoa.domain.user.entity.User;

@Schema(description = "회원가입 응답")
public record SignUpResponse(
        @Schema(description = "생성된 사용자 ID", example = "1") Long userId,
        @Schema(description = "이메일", example = "user@example.com") String email,
        @Schema(description = "닉네임", example = "동네주민") String nickname
) {
    public static SignUpResponse from(User user) {
        return new SignUpResponse(user.getId(), user.getEmail(), user.getNickname());
    }
}
