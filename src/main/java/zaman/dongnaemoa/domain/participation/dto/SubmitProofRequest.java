package zaman.dongnaemoa.domain.participation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "퀘스트 완료 인증 제출 요청")
public record SubmitProofRequest(
        @Schema(description = "인증 설명", example = "쓰레기를 다 치웠습니다.") String proofDescription
) {
}
