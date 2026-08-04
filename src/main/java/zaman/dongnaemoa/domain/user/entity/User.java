package zaman.dongnaemoa.domain.user.entity;

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
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import zaman.dongnaemoa.domain.neighborhood.entity.Neighborhood;
import zaman.dongnaemoa.global.entity.BaseTimeEntity;

@Getter
@Entity
@Table(name = "app_user")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String nickname;

    @Column(nullable = false)
    private Integer point;

    @Column(nullable = false)
    private Integer xp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "neighborhood_id")
    private Neighborhood neighborhood;

    @Version
    private Long version;

    @Builder
    private User(String email, String password, String nickname, Neighborhood neighborhood) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.neighborhood = neighborhood;
        this.point = 0;
        this.xp = 0;
        this.role = Role.USER;
    }

    public void moveTo(Neighborhood neighborhood) {
        this.neighborhood = neighborhood;
    }

    public void gainReward(int pointAmount, int xpAmount) {
        this.point += pointAmount;
        this.xp += xpAmount;
    }
}
