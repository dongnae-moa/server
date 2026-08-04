package zaman.dongnaemoa.domain.user.dto;

import zaman.dongnaemoa.domain.user.entity.User;

public record SignUpResponse(
        Long userId,
        String email,
        String nickname
) {
    public static SignUpResponse from(User user) {
        return new SignUpResponse(user.getId(), user.getEmail(), user.getNickname());
    }
}
