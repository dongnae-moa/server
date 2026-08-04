package zaman.dongnaemoa.domain.reward.entity;

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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import zaman.dongnaemoa.domain.participation.entity.QuestParticipation;
import zaman.dongnaemoa.domain.user.entity.User;
import zaman.dongnaemoa.global.entity.BaseTimeEntity;

@Getter
@Entity
@Table(name = "point_history")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointHistory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quest_participation_id")
    private QuestParticipation questParticipation;

    @Column(nullable = false)
    private Integer pointAmount;

    @Column(nullable = false)
    private Integer xpAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PointReason reason;

    @Builder
    private PointHistory(User user, QuestParticipation questParticipation, Integer pointAmount,
                          Integer xpAmount, PointReason reason) {
        this.user = user;
        this.questParticipation = questParticipation;
        this.pointAmount = pointAmount;
        this.xpAmount = xpAmount;
        this.reason = reason;
    }
}
