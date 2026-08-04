package zaman.dongnaemoa.domain.quest.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import zaman.dongnaemoa.domain.quest.entity.Quest;

public interface QuestRepository extends JpaRepository<Quest, Long> {

    List<Quest> findByNeighborhoodId(Long neighborhoodId);
}
