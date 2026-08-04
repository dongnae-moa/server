package zaman.dongnaemoa.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "회원가입 요청")
public record SignUpRequest(
        @Schema(description = "이메일 (로그인 ID로 사용)", example = "user@example.com")
        @NotBlank @Email String email,

        @Schema(description = "비밀번호 (8~64자)", example = "password123")
        @NotBlank @Size(min = 8, max = 64) String password,

        @Schema(description = "닉네임 (2~20자, 중복 불가)", example = "동네주민")
        @NotBlank @Size(min = 2, max = 20) String nickname
) {
}
