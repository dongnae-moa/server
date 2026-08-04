package zaman.dongnaemoa.domain.neighborhood.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team.themoment.sdk.exception.ExpectedException;
import zaman.dongnaemoa.domain.neighborhood.dto.JoinNeighborhoodRequest;
import zaman.dongnaemoa.domain.neighborhood.dto.NeighborhoodResponse;
import zaman.dongnaemoa.domain.neighborhood.entity.Neighborhood;
import zaman.dongnaemoa.domain.neighborhood.repository.NeighborhoodRepository;
import zaman.dongnaemoa.domain.user.entity.User;
import zaman.dongnaemoa.domain.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class NeighborhoodServiceTest {

    @Mock
    private NeighborhoodRepository neighborhoodRepository;

    @Mock
    private UserRepository userRepository;

    private NeighborhoodService neighborhoodService;

    @BeforeEach
    void setUp() {
        neighborhoodService = new NeighborhoodService(neighborhoodRepository, userRepository);
    }

    private Neighborhood neighborhood(String name, double lat, double lon) {
        return Neighborhood.builder()
                .administrativeCode(name + "-code")
                .name(name)
                .sido("서울특별시")
                .sigungu("종로구")
                .latitude(BigDecimal.valueOf(lat))
                .longitude(BigDecimal.valueOf(lon))
                .build();
    }

    @Test
    @DisplayName("가장 가까운 동네를 찾아 사용자를 이동시킨다")
    void join_movesUserToNearestNeighborhood() {
        User user = User.builder().email("user@test.com").password("pw").nickname("nick").neighborhood(null).build();
        Neighborhood near = neighborhood("청운동", 37.5800, 126.9700);
        Neighborhood far = neighborhood("여의도동", 37.5219, 126.9245);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(neighborhoodRepository.findAll()).thenReturn(List.of(far, near));

        NeighborhoodResponse response = neighborhoodService.join(1L, new JoinNeighborhoodRequest(37.5805, 126.9705));

        assertThat(response.name()).isEqualTo("청운동");
        assertThat(user.getNeighborhood()).isEqualTo(near);
    }

    @Test
    @DisplayName("존재하지 않는 사용자면 NOT_FOUND 예외를 던진다")
    void join_userNotFound_throwsNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> neighborhoodService.join(1L, new JoinNeighborhoodRequest(37.5, 127.0)))
                .isInstanceOf(ExpectedException.class)
                .hasMessageContaining("사용자");
    }

    @Test
    @DisplayName("등록된 동네가 없으면 NOT_FOUND 예외를 던진다")
    void join_noNeighborhoods_throwsNotFound() {
        User user = User.builder().email("user@test.com").password("pw").nickname("nick").neighborhood(null).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(neighborhoodRepository.findAll()).thenReturn(List.of());

        assertThatThrownBy(() -> neighborhoodService.join(1L, new JoinNeighborhoodRequest(37.5, 127.0)))
                .isInstanceOf(ExpectedException.class)
                .hasMessageContaining("동네가 없습니다");
    }

    @Test
    @DisplayName("전체 동네 목록을 조회한다")
    void findAll_returnsAllNeighborhoods() {
        when(neighborhoodRepository.findAll())
                .thenReturn(List.of(neighborhood("청운동", 37.58, 126.97), neighborhood("여의도동", 37.52, 126.92)));

        List<NeighborhoodResponse> responses = neighborhoodService.findAll();

        assertThat(responses).hasSize(2)
                .extracting(NeighborhoodResponse::name)
                .containsExactlyInAnyOrder("청운동", "여의도동");
    }

    @Test
    @DisplayName("동네 ID로 단건 조회한다")
    void findById_existingId_returnsNeighborhood() {
        Neighborhood neighborhood = neighborhood("청운동", 37.58, 126.97);
        when(neighborhoodRepository.findById(1L)).thenReturn(Optional.of(neighborhood));

        NeighborhoodResponse response = neighborhoodService.findById(1L);

        assertThat(response.name()).isEqualTo("청운동");
    }

    @Test
    @DisplayName("존재하지 않는 동네 ID면 NOT_FOUND 예외를 던진다")
    void findById_missingId_throwsNotFound() {
        when(neighborhoodRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> neighborhoodService.findById(999L))
                .isInstanceOf(ExpectedException.class)
                .hasMessageContaining("동네를 찾을 수 없습니다");
    }
}
