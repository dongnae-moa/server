package zaman.dongnaemoa.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import team.themoment.sdk.exception.ExpectedException;
import zaman.dongnaemoa.domain.neighborhood.entity.Neighborhood;
import zaman.dongnaemoa.domain.user.dto.UserResponse;
import zaman.dongnaemoa.domain.user.entity.User;
import zaman.dongnaemoa.domain.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository);
    }

    private Neighborhood neighborhood() {
        return Neighborhood.builder()
                .administrativeCode("code")
                .name("청운동")
                .sido("서울특별시")
                .sigungu("종로구")
                .latitude(BigDecimal.valueOf(37.58))
                .longitude(BigDecimal.valueOf(126.97))
                .build();
    }

    @Test
    @DisplayName("가입한 동네가 있는 사용자의 정보를 조회한다")
    void getMe_userWithNeighborhood_returnsUserResponse() {
        User user = User.builder().email("a@test.com").password("pw").nickname("nick")
                .neighborhood(neighborhood()).build();
        ReflectionTestUtils.setField(user, "id", 1L);
        ReflectionTestUtils.setField(user, "point", 150);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponse response = userService.getMe(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("a@test.com");
        assertThat(response.nickname()).isEqualTo("nick");
        assertThat(response.point()).isEqualTo(150);
        assertThat(response.neighborhood().name()).isEqualTo("청운동");
    }

    @Test
    @DisplayName("동네에 가입하지 않은 사용자는 neighborhood가 null로 조회된다")
    void getMe_userWithoutNeighborhood_returnsNullNeighborhood() {
        User user = User.builder().email("a@test.com").password("pw").nickname("nick")
                .neighborhood(null).build();
        ReflectionTestUtils.setField(user, "id", 1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponse response = userService.getMe(1L);

        assertThat(response.neighborhood()).isNull();
    }

    @Test
    @DisplayName("존재하지 않는 사용자는 조회할 수 없다")
    void getMe_userNotFound_throwsNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getMe(1L))
                .isInstanceOf(ExpectedException.class)
                .hasMessageContaining("사용자를 찾을 수 없습니다");
    }
}
