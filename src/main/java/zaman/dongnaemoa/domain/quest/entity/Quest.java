package zaman.dongnaemoa.domain.quest.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import zaman.dongnaemoa.domain.neighborhood.entity.Neighborhood;
import zaman.dongnaemoa.domain.user.entity.User;
import zaman.dongnaemoa.global.entity.BaseTimeEntity;

@Getter
@Entity
@Table(name = "quest")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Quest extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String imageUrl;

    @Column(nullable = false)
    private Integer rewardPoint;

    private Integer minutes;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private QuestDifficulty difficulty;

    @ElementCollection
    @CollectionTable(name = "quest_checkpoint", joinColumns = @JoinColumn(name = "quest_id"))
    @Column(name = "content")
    private List<String> checkpoints = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private QuestStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "neighborhood_id", nullable = false)
    private Neighborhood neighborhood;

    @Column(nullable = false, precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 9, scale = 6)
    private BigDecimal longitude;

    @Builder
    private Quest(String title, String description, String imageUrl, Integer rewardPoint,
                   Integer minutes, QuestDifficulty difficulty, List<String> checkpoints,
                   User author, Neighborhood neighborhood, BigDecimal latitude, BigDecimal longitude) {
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.rewardPoint = rewardPoint;
        this.minutes = minutes;
        this.difficulty = difficulty;
        this.checkpoints = checkpoints == null ? new ArrayList<>() : checkpoints;
        this.author = author;
        this.neighborhood = neighborhood;
        this.latitude = latitude;
        this.longitude = longitude;
        this.status = QuestStatus.RECRUITING;
    }

    public void changeStatus(QuestStatus status) {
        this.status = status;
    }
}
