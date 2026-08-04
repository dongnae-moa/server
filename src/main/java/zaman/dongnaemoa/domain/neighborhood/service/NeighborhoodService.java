package zaman.dongnaemoa.domain.neighborhood.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.themoment.sdk.exception.ExpectedException;
import zaman.dongnaemoa.domain.neighborhood.dto.JoinNeighborhoodRequest;
import zaman.dongnaemoa.domain.neighborhood.dto.NeighborhoodResponse;
import zaman.dongnaemoa.domain.neighborhood.entity.Neighborhood;
import zaman.dongnaemoa.domain.neighborhood.repository.NeighborhoodRepository;
import zaman.dongnaemoa.domain.user.entity.User;
import zaman.dongnaemoa.domain.user.repository.UserRepository;
import zaman.dongnaemoa.global.geo.GeoUtils;

@Service
@RequiredArgsConstructor
public class NeighborhoodService {

    private final NeighborhoodRepository neighborhoodRepository;
    private final UserRepository userRepository;

    @Value("${neighborhood.join.max-distance-meters:5000}")
    private double maxJoinDistanceMeters;

    @Transactional
    public NeighborhoodResponse join(Long userId, JoinNeighborhoodRequest request) {
        if (!Double.isFinite(request.latitude()) || !Double.isFinite(request.longitude())) {
            throw new ExpectedException("유효하지 않은 좌표입니다.", HttpStatus.BAD_REQUEST);
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ExpectedException("사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        Neighborhood nearest = findNearest(request.latitude(), request.longitude());
        double distance = GeoUtils.distanceMeters(request.latitude(), request.longitude(),
                nearest.getLatitude().doubleValue(), nearest.getLongitude().doubleValue());
        if (distance > maxJoinDistanceMeters) {
            throw new ExpectedException("가입 가능한 동네가 근처에 없습니다.", HttpStatus.BAD_REQUEST);
        }
        user.moveTo(nearest);
        return NeighborhoodResponse.from(nearest);
    }

    @Transactional(readOnly = true)
    public List<NeighborhoodResponse> findAll() {
        return neighborhoodRepository.findAll().stream()
                .map(NeighborhoodResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public NeighborhoodResponse findById(Long neighborhoodId) {
        Neighborhood neighborhood = neighborhoodRepository.findById(neighborhoodId)
                .orElseThrow(() -> new ExpectedException("동네를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        return NeighborhoodResponse.from(neighborhood);
    }

    private Neighborhood findNearest(double latitude, double longitude) {
        List<Neighborhood> all = neighborhoodRepository.findAll();
        if (all.isEmpty()) {
            throw new ExpectedException("등록된 동네가 없습니다.", HttpStatus.NOT_FOUND);
        }

        return all.stream()
                .min((a, b) -> Double.compare(
                        GeoUtils.distanceMeters(latitude, longitude,
                                a.getLatitude().doubleValue(), a.getLongitude().doubleValue()),
                        GeoUtils.distanceMeters(latitude, longitude,
                                b.getLatitude().doubleValue(), b.getLongitude().doubleValue())))
                .orElseThrow();
    }
}
