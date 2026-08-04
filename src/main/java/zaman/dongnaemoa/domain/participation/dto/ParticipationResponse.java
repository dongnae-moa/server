package zaman.dongnaemoa.domain.participation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import zaman.dongnaemoa.domain.participation.entity.ParticipationStatus;
import zaman.dongnaemoa.domain.participation.entity.QuestParticipation;

@Schema(description = "퀘스트 참여 정보 응답")
public record ParticipationResponse(
        @Schema(description = "참여 ID", example = "1") Long id,
        @Schema(description = "참여한 퀘스트 ID", example = "1") Long questId,
        @Schema(description = "참여자 사용자 ID", example = "2") Long participantId,
        @Schema(description = "참여 상태 (JOINED: 참여함, SUBMITTED: 인증 제출됨, "
                + "APPROVED: 승인(완료), REJECTED: 반려)") ParticipationStatus status,
        @Schema(description = "제출한 인증 이미지 URL") String proofImageUrl,
        @Schema(description = "제출한 인증 설명") String proofDescription,
        @Schema(description = "반려 사유 (반려된 경우에만 존재)") String rejectionReason
) {
    public static ParticipationResponse from(QuestParticipation participation) {
        return new ParticipationResponse(
                participation.getId(),
                participation.getQuest().getId(),
                participation.getParticipant().getId(),
                participation.getStatus(),
                participation.getProofImageUrl(),
                participation.getProofDescription(),
                participation.getRejectionReason());
    }
}
