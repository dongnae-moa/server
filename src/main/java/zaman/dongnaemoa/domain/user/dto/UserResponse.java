package zaman.dongnaemoa.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import zaman.dongnaemoa.domain.neighborhood.entity.Neighborhood;
import zaman.dongnaemoa.domain.user.entity.User;

@Schema(description = "내 정보 응답")
public record UserResponse(
        @Schema(description = "사용자 ID", example = "1") Long id,
        @Schema(description = "이메일", example = "user@example.com") String email,
        @Schema(description = "닉네임", example = "동네주민") String nickname,
        @Schema(description = "보유 포인트", example = "150") Integer point,
        @Schema(description = "가입한 동네 정보. 아직 가입하지 않았다면 null") NeighborhoodSummary neighborhood
) {
    @Schema(description = "동네 요약 정보")
    public record NeighborhoodSummary(
            @Schema(description = "동네 이름", example = "청운동") String name,
            @Schema(description = "시/도", example = "서울특별시") String sido,
            @Schema(description = "시/군/구", example = "종로구") String sigungu
    ) {
        public static NeighborhoodSummary from(Neighborhood neighborhood) {
            return new NeighborhoodSummary(neighborhood.getName(), neighborhood.getSido(), neighborhood.getSigungu());
        }
    }

    public static UserResponse from(User user) {
        Neighborhood neighborhood = user.getNeighborhood();
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getPoint(),
                neighborhood == null ? null : NeighborhoodSummary.from(neighborhood));
    }
}
