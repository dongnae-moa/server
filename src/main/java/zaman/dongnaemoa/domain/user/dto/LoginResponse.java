package zaman.dongnaemoa.domain.user.dto;

import zaman.dongnaemoa.domain.user.entity.User;

public record LoginResponse(
        String accessToken,
        String tokenType,
        Long userId,
        String nickname
) {
    public static LoginResponse of(String accessToken, User user) {
        return new LoginResponse(accessToken, "Bearer", user.getId(), user.getNickname());
    }
}
