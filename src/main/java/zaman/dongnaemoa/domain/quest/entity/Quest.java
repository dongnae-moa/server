package zaman.dongnaemoa.domain.quest.entity;

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private QuestStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "neighborhood_id", nullable = false)
    private Neighborhood neighborhood;

    @Builder
    private Quest(String title, String description, String imageUrl, Integer rewardPoint,
                   User author, Neighborhood neighborhood) {
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.rewardPoint = rewardPoint;
        this.author = author;
        this.neighborhood = neighborhood;
        this.status = QuestStatus.RECRUITING;
    }

    public void changeStatus(QuestStatus status) {
        this.status = status;
    }
}
