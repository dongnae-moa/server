package zaman.dongnaemoa.domain.neighborhood.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import zaman.dongnaemoa.global.entity.BaseTimeEntity;

@Getter
@Entity
@Table(name = "neighborhood")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Neighborhood extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String administrativeCode;

    @Column(nullable = false)
    private String name;

    private String sido;

    private String sigungu;

    @Column(nullable = false, precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 9, scale = 6)
    private BigDecimal longitude;

    @Version
    private Long version;

    @Builder
    private Neighborhood(String administrativeCode, String name, String sido, String sigungu,
                          BigDecimal latitude, BigDecimal longitude) {
        this.administrativeCode = administrativeCode;
        this.name = name;
        this.sido = sido;
        this.sigungu = sigungu;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
