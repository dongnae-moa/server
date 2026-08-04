package zaman.dongnaemoa.domain.neighborhood.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import zaman.dongnaemoa.domain.neighborhood.entity.Neighborhood;

public interface NeighborhoodRepository extends JpaRepository<Neighborhood, Long> {

    Optional<Neighborhood> findByAdministrativeCode(String administrativeCode);
}
