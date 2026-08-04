package zaman.dongnaemoa.domain.reward.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import zaman.dongnaemoa.domain.reward.entity.PointHistory;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {

    List<PointHistory> findByUserId(Long userId);
}
