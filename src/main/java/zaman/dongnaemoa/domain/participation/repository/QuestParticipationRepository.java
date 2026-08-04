package zaman.dongnaemoa.domain.participation.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import zaman.dongnaemoa.domain.participation.entity.QuestParticipation;

public interface QuestParticipationRepository extends JpaRepository<QuestParticipation, Long> {

    List<QuestParticipation> findByQuestId(Long questId);

    Optional<QuestParticipation> findByQuestIdAndParticipantId(Long questId, Long participantId);
}
