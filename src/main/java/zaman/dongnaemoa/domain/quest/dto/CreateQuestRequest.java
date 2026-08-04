package zaman.dongnaemoa.domain.quest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "퀘스트 등록 요청 (로그인한 사용자가 가입한 동네 단위로 등록됨). "
        + "예상 소요시간/보상 포인트/난이도/확인 목록은 AI가 자동으로 산정하므로 별도 입력하지 않는다.")
public record CreateQuestRequest(
        @Schema(description = "퀘스트 제목 (최대 100자)", example = "공원 쓰레기 치우기")
        @NotBlank @Size(max = 100) String title,

        @Schema(description = "퀘스트 상세 설명", example = "OO공원 벤치 주변에 버려진 쓰레기를 치우고 인증샷을 남겨주세요.")
        String description,

        @Schema(description = "퀘스트가 진행되는 위치의 위도", example = "37.5665")
        @NotNull Double latitude,

        @Schema(description = "퀘스트가 진행되는 위치의 경도", example = "126.9780")
        @NotNull Double longitude
) {
}
