package zaman.dongnaemoa.domain.neighborhood.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
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

@Service
@RequiredArgsConstructor
public class NeighborhoodService {

    private static final int EARTH_RADIUS_METERS = 6371000;

    private final NeighborhoodRepository neighborhoodRepository;
    private final UserRepository userRepository;

    @Transactional
    public NeighborhoodResponse join(Long userId, JoinNeighborhoodRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ExpectedException("사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        Neighborhood nearest = findNearest(request.latitude(), request.longitude());
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
                        distance(latitude, longitude, a),
                        distance(latitude, longitude, b)))
                .orElseThrow();
    }

    private double distance(double latitude, double longitude, Neighborhood neighborhood) {
        double lat1 = Math.toRadians(latitude);
        double lat2 = Math.toRadians(neighborhood.getLatitude().doubleValue());
        double deltaLat = Math.toRadians(neighborhood.getLatitude().doubleValue() - latitude);
        double deltaLon = Math.toRadians(neighborhood.getLongitude().doubleValue() - longitude);

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_METERS * c;
    }
}
