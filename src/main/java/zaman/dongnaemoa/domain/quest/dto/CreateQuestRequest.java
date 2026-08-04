package zaman.dongnaemoa.domain.quest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Schema(description = "퀘스트 등록 요청 (로그인한 사용자가 가입한 동네 단위로 등록됨)")
public record CreateQuestRequest(
        @Schema(description = "퀘스트 제목 (최대 100자)", example = "공원 쓰레기 치우기")
        @NotBlank @Size(max = 100) String title,

        @Schema(description = "퀘스트 상세 설명", example = "OO공원 벤치 주변에 버려진 쓰레기를 치우고 인증샷을 남겨주세요.")
        String description,

        @Schema(description = "완료 시 지급할 보상 포인트", example = "500")
        @NotNull @Positive Integer rewardPoint
) {
}
