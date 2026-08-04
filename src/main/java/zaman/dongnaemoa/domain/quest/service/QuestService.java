package zaman.dongnaemoa.domain.quest.service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import team.themoment.sdk.exception.ExpectedException;
import zaman.dongnaemoa.domain.quest.dto.CreateQuestRequest;
import zaman.dongnaemoa.domain.quest.dto.QuestResponse;
import zaman.dongnaemoa.domain.quest.entity.Quest;
import zaman.dongnaemoa.domain.quest.entity.QuestDifficulty;
import zaman.dongnaemoa.domain.quest.repository.QuestRepository;
import zaman.dongnaemoa.domain.user.entity.User;
import zaman.dongnaemoa.domain.user.repository.UserRepository;
import zaman.dongnaemoa.global.ai.GroqQuestAnalyzer;
import zaman.dongnaemoa.global.ai.GroqQuestAnalyzer.QuestAnalysisResult;
import zaman.dongnaemoa.global.geo.GeoUtils;
import zaman.dongnaemoa.global.storage.FileStorageService;

@Service
@RequiredArgsConstructor
public class QuestService {

    private final QuestRepository questRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final GroqQuestAnalyzer groqQuestAnalyzer;

    @Transactional
    public QuestResponse create(Long authorId, CreateQuestRequest request, MultipartFile image) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new ExpectedException("사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        if (author.getNeighborhood() == null) {
            throw new ExpectedException("동네에 가입해야 퀘스트를 등록할 수 있습니다.", HttpStatus.BAD_REQUEST);
        }

        String imageUrl = (image == null || image.isEmpty()) ? null : fileStorageService.store(image);
        QuestAnalysisResult analysis = groqQuestAnalyzer.analyze(request.title(), request.description());

        Quest quest = Quest.builder()
                .title(request.title())
                .description(request.description())
                .imageUrl(imageUrl)
                .rewardPoint(analysis.rewardPoint())
                .minutes(analysis.minutes())
                .difficulty(parseDifficulty(analysis.difficulty()))
                .checkpoints(analysis.checkpoints())
                .author(author)
                .neighborhood(author.getNeighborhood())
                .latitude(BigDecimal.valueOf(request.latitude()))
                .longitude(BigDecimal.valueOf(request.longitude()))
                .build();

        return QuestResponse.from(questRepository.save(quest));
    }

    private QuestDifficulty parseDifficulty(String difficulty) {
        try {
            return QuestDifficulty.valueOf(difficulty);
        } catch (Exception e) {
            return QuestDifficulty.NORMAL;
        }
    }

    @Transactional(readOnly = true)
    public List<QuestResponse> findByNeighborhood(Long neighborhoodId, double latitude, double longitude) {
        return questRepository.findByNeighborhoodId(neighborhoodId).stream()
                .map(quest -> QuestResponse.from(quest, GeoUtils.distanceMeters(
                        latitude, longitude,
                        quest.getLatitude().doubleValue(), quest.getLongitude().doubleValue())))
                .sorted(Comparator.comparingDouble(QuestResponse::distanceMeters))
                .toList();
    }

    @Transactional
    public void delete(Long userId, Long questId) {
        Quest quest = questRepository.findById(questId)
                .orElseThrow(() -> new ExpectedException("퀘스트를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        if (!quest.getAuthor().getId().equals(userId)) {
            throw new ExpectedException("본인이 등록한 퀘스트만 삭제할 수 있습니다.", HttpStatus.FORBIDDEN);
        }
        questRepository.delete(quest);
    }
}
