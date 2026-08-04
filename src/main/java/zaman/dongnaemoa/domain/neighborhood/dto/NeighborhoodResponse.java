package zaman.dongnaemoa.domain.neighborhood.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import zaman.dongnaemoa.domain.neighborhood.entity.Neighborhood;

@Schema(description = "동네 정보 응답")
public record NeighborhoodResponse(
        @Schema(description = "동네 ID", example = "1") Long id,
        @Schema(description = "행정표준코드", example = "1111010100") String administrativeCode,
        @Schema(description = "동네 이름", example = "청운동") String name,
        @Schema(description = "시/도", example = "서울특별시") String sido,
        @Schema(description = "시/군/구", example = "종로구") String sigungu
) {
    public static NeighborhoodResponse from(Neighborhood neighborhood) {
        return new NeighborhoodResponse(
                neighborhood.getId(),
                neighborhood.getAdministrativeCode(),
                neighborhood.getName(),
                neighborhood.getSido(),
                neighborhood.getSigungu());
    }
}
