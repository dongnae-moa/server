package zaman.dongnaemoa.domain.participation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "퀘스트 참여 반려 요청")
public record RejectParticipationRequest(
        @Schema(description = "반려 사유", example = "인증 사진이 퀘스트 내용과 맞지 않습니다.")
        @NotBlank String rejectionReason
) {
}
