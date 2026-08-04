package zaman.dongnaemoa.domain.quest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;
import zaman.dongnaemoa.domain.neighborhood.entity.Neighborhood;
import zaman.dongnaemoa.domain.quest.entity.Quest;
import zaman.dongnaemoa.domain.quest.entity.QuestDifficulty;
import zaman.dongnaemoa.domain.quest.entity.QuestStatus;

@Schema(description = "퀘스트 정보 응답")
public record QuestResponse(
        @Schema(description = "퀘스트 ID", example = "1") Long id,
        @Schema(description = "제목", example = "공원 쓰레기 치우기") String title,
        @Schema(description = "상세 설명") String description,
        @Schema(description = "첨부 이미지 URL") String imageUrl,
        @Schema(description = "보상 포인트 (AI 산정)", example = "30") Integer rewardPoint,
        @Schema(description = "퀘스트 상태 (RECRUITING: 모집중, IN_PROGRESS: 진행중, "
                + "PENDING_REVIEW: 인증 검토중, COMPLETED: 완료, CANCELLED: 취소)") QuestStatus status,
        @Schema(description = "예상 소요시간(분) (AI 산정)", example = "6") Integer minutes,
        @Schema(description = "난이도: 쉬움/보통/어려움 (AI 산정)", example = "보통") String difficulty,
        @Schema(description = "인증 시 확인할 체크리스트 (AI 산정)") List<String> checkpoints,
        @Schema(description = "등록자(작성자) 닉네임", example = "글쓴사람 닉네임") String authorNickname,
        @Schema(description = "등록된 동네 정보") NeighborhoodSummary neighborhood,
        @Schema(description = "퀘스트 위치 위도", example = "37.5665") BigDecimal latitude,
        @Schema(description = "퀘스트 위치 경도", example = "126.9780") BigDecimal longitude,
        @Schema(description = "현재 위치로부터의 거리(미터). 목록 조회(GET /v1/quests) 응답에만 포함되며, "
                + "등록 응답에서는 null이다.", example = "380") Double distanceMeters
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

    public static QuestResponse from(Quest quest) {
        return from(quest, null);
    }

    public static QuestResponse from(Quest quest, Double distanceMeters) {
        return new QuestResponse(
                quest.getId(),
                quest.getTitle(),
                quest.getDescription(),
                quest.getImageUrl(),
                quest.getRewardPoint(),
                quest.getStatus(),
                quest.getMinutes(),
                toKorean(quest.getDifficulty()),
                List.copyOf(quest.getCheckpoints()),
                quest.getAuthor().getNickname(),
                NeighborhoodSummary.from(quest.getNeighborhood()),
                quest.getLatitude(),
                quest.getLongitude(),
                distanceMeters);
    }

    private static String toKorean(QuestDifficulty difficulty) {
        if (difficulty == null) {
            return null;
        }
        return switch (difficulty) {
            case EASY -> "쉬움";
            case NORMAL -> "보통";
            case HARD -> "어려움";
        };
    }
}
