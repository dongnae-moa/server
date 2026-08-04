package zaman.dongnaemoa.domain.participation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import zaman.dongnaemoa.domain.quest.entity.Quest;
import zaman.dongnaemoa.domain.user.entity.User;
import zaman.dongnaemoa.global.entity.BaseTimeEntity;

@Getter
@Entity
@Table(
        name = "quest_participation",
        uniqueConstraints = @UniqueConstraint(columnNames = {"quest_id", "user_id"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuestParticipation extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quest_id", nullable = false)
    private Quest quest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User participant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ParticipationStatus status;

    private String proofImageUrl;

    @Column(columnDefinition = "TEXT")
    private String proofDescription;

    private LocalDateTime submittedAt;

    private LocalDateTime decidedAt;

    private String rejectionReason;

    @Builder
    private QuestParticipation(Quest quest, User participant) {
        this.quest = quest;
        this.participant = participant;
        this.status = ParticipationStatus.JOINED;
    }

    public void submitProof(String proofImageUrl, String proofDescription, LocalDateTime submittedAt) {
        this.proofImageUrl = proofImageUrl;
        this.proofDescription = proofDescription;
        this.submittedAt = submittedAt;
        this.status = ParticipationStatus.SUBMITTED;
    }

    public void approve(LocalDateTime decidedAt) {
        this.status = ParticipationStatus.APPROVED;
        this.decidedAt = decidedAt;
    }

    public void reject(String rejectionReason, LocalDateTime decidedAt) {
        this.status = ParticipationStatus.REJECTED;
        this.rejectionReason = rejectionReason;
        this.decidedAt = decidedAt;
    }
}
