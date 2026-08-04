package zaman.dongnaemoa.domain.quest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import zaman.dongnaemoa.domain.quest.entity.Quest;
import zaman.dongnaemoa.domain.quest.entity.QuestStatus;

@Schema(description = "퀘스트 정보 응답")
public record QuestResponse(
        @Schema(description = "퀘스트 ID", example = "1") Long id,
        @Schema(description = "제목", example = "공원 쓰레기 치우기") String title,
        @Schema(description = "상세 설명") String description,
        @Schema(description = "첨부 이미지 URL") String imageUrl,
        @Schema(description = "보상 포인트", example = "500") Integer rewardPoint,
        @Schema(description = "퀘스트 상태 (RECRUITING: 모집중, IN_PROGRESS: 진행중, "
                + "PENDING_REVIEW: 인증 검토중, COMPLETED: 완료, CANCELLED: 취소)") QuestStatus status,
        @Schema(description = "등록자(작성자) 사용자 ID", example = "1") Long authorId,
        @Schema(description = "등록된 동네 ID", example = "1") Long neighborhoodId
) {
    public static QuestResponse from(Quest quest) {
        return new QuestResponse(
                quest.getId(),
                quest.getTitle(),
                quest.getDescription(),
                quest.getImageUrl(),
                quest.getRewardPoint(),
                quest.getStatus(),
                quest.getAuthor().getId(),
                quest.getNeighborhood().getId());
    }
}
