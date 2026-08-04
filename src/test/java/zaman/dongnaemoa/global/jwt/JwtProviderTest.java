package zaman.dongnaemoa.global.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import zaman.dongnaemoa.domain.user.entity.Role;

class JwtProviderTest {

    private static final String TEST_SECRET = "test-secret-key-for-jwt-provider-unit-test-must-be-long-enough";

    private JwtProvider jwtProvider;

    @BeforeEach
    void setUp() {
        JwtProperties longLivedProperties = new JwtProperties(TEST_SECRET, 3_600_000L);
        jwtProvider = new JwtProvider(longLivedProperties);
    }

    @Test
    @DisplayName("토큰을 생성하면 유효한 토큰으로 검증된다")
    void generateAccessToken_thenValidateToken_returnsTrue() {
        String token = jwtProvider.generateAccessToken(1L, "user@test.com", Role.USER);

        assertThat(jwtProvider.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("생성한 토큰에서 subject로 넣은 userId를 그대로 추출할 수 있다")
    void generateAccessToken_thenGetUserId_returnsOriginalUserId() {
        String token = jwtProvider.generateAccessToken(42L, "user@test.com", Role.USER);

        assertThat(jwtProvider.getUserId(token)).isEqualTo(42L);
    }

    @Test
    @DisplayName("만료된 토큰은 검증에 실패한다")
    void validateToken_expiredToken_returnsFalse() {
        JwtProperties expiredProperties = new JwtProperties(TEST_SECRET, -1_000L);
        JwtProvider expiredTokenProvider = new JwtProvider(expiredProperties);
        String expiredToken = expiredTokenProvider.generateAccessToken(1L, "user@test.com", Role.USER);

        assertThat(jwtProvider.validateToken(expiredToken)).isFalse();
    }

    @Test
    @DisplayName("형식이 올바르지 않은 토큰은 검증에 실패한다")
    void validateToken_malformedToken_returnsFalse() {
        assertThat(jwtProvider.validateToken("not-a-valid-jwt-token")).isFalse();
    }
}
