package zaman.dongnaemoa.domain.neighborhood.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

@Schema(description = "동네 가입 요청 (GPS 좌표로 가장 가까운 동네를 찾아 가입시킴)")
public record JoinNeighborhoodRequest(
        @Schema(description = "현재 위치 위도", example = "37.5665")
        @NotNull @DecimalMin("-90") @DecimalMax("90") Double latitude,

        @Schema(description = "현재 위치 경도", example = "126.9780")
        @NotNull @DecimalMin("-180") @DecimalMax("180") Double longitude
) {
}
