package zaman.dongnaemoa.global.geo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GeoUtilsTest {

    @Test
    @DisplayName("동일한 좌표 간 거리는 0이다")
    void distanceMeters_samePoint_returnsZero() {
        double distance = GeoUtils.distanceMeters(37.5665, 126.9780, 37.5665, 126.9780);

        assertThat(distance).isZero();
    }

    @Test
    @DisplayName("서울시청과 부산시청 간 거리를 오차범위 내로 계산한다")
    void distanceMeters_seoulToBusan_returnsApproximateDistance() {
        double distance = GeoUtils.distanceMeters(37.5665, 126.9780, 35.1796, 129.0756);

        assertThat(distance).isCloseTo(325_000, within(5_000.0));
    }
}
