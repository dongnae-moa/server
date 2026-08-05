package zaman.dongnaemoa.domain.participation.service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import team.themoment.sdk.exception.ExpectedException;
import zaman.dongnaemoa.domain.participation.dto.ParticipationResponse;
import zaman.dongnaemoa.domain.participation.dto.SubmitProofRequest;
import zaman.dongnaemoa.domain.participation.entity.QuestParticipation;
import zaman.dongnaemoa.domain.participation.repository.QuestParticipationRepository;
import zaman.dongnaemoa.domain.quest.entity.Quest;
import zaman.dongnaemoa.domain.quest.entity.QuestStatus;
import zaman.dongnaemoa.domain.quest.repository.QuestRepository;
import zaman.dongnaemoa.domain.reward.entity.PointHistory;
import zaman.dongnaemoa.domain.reward.entity.PointReason;
import zaman.dongnaemoa.domain.reward.repository.PointHistoryRepository;
import zaman.dongnaemoa.domain.user.entity.User;
import zaman.dongnaemoa.domain.user.repository.UserRepository;
import zaman.dongnaemoa.global.storage.FileStorageService;

@Service
@RequiredArgsConstructor
public class ParticipationService {

    private final QuestParticipationRepository participationRepository;
    private final QuestRepository questRepository;
    private final UserRepository userRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final FileStorageService fileStorageService;

    @Transactional
    public ParticipationResponse join(Long userId, Long questId) {
        Quest quest = getQuest(questId);
        User participant = getUser(userId);

        if (quest.getAuthor().getId().equals(userId)) {
            throw new ExpectedException("본인이 등록한 퀘스트에는 참여할 수 없습니다.", HttpStatus.BAD_REQUEST);
        }
        if (quest.getStatus() != QuestStatus.RECRUITING) {
            throw new ExpectedException("모집 중인 퀘스트에만 참여할 수 있습니다.", HttpStatus.CONFLICT);
        }
        participationRepository.findByQuestIdAndParticipantId(questId, userId).ifPresent(p -> {
            throw new ExpectedException("이미 참여한 퀘스트입니다.", HttpStatus.CONFLICT);
        });

        QuestParticipation participation = QuestParticipation.builder()
                .quest(quest)
                .participant(participant)
                .build();
        return ParticipationResponse.from(participationRepository.save(participation));
    }

    @Transactional
    public ParticipationResponse submitProof(Long userId, Long participationId, SubmitProofRequest request,
                                              MultipartFile proofImage) {
        QuestParticipation participation = getParticipation(participationId);
        if (!participation.getParticipant().getId().equals(userId)) {
            throw new ExpectedException("본인의 참여 건만 인증할 수 있습니다.", HttpStatus.FORBIDDEN);
        }
        String proofImageUrl = (proofImage == null || proofImage.isEmpty())
                ? null : fileStorageService.store(proofImage);
        try {
            participation.submitProof(proofImageUrl, request.proofDescription(), LocalDateTime.now());
        } catch (IllegalStateException e) {
            throw new ExpectedException(e.getMessage(), HttpStatus.CONFLICT);
        }
        return ParticipationResponse.from(participation);
    }

    @Transactional
    public ParticipationResponse approve(Long authorId, Long participationId) {
        QuestParticipation participation = getParticipation(participationId);
        Quest quest = participation.getQuest();
        if (!quest.getAuthor().getId().equals(authorId)) {
            throw new ExpectedException("퀘스트 등록자만 완료 여부를 결정할 수 있습니다.", HttpStatus.FORBIDDEN);
        }

        try {
            participation.approve(LocalDateTime.now());
        } catch (IllegalStateException e) {
            throw new ExpectedException(e.getMessage(), HttpStatus.CONFLICT);
        }

        User participant = participation.getParticipant();
        participant.gainPoint(quest.getRewardPoint());
        pointHistoryRepository.save(PointHistory.builder()
                .user(participant)
                .questParticipation(participation)
                .pointAmount(quest.getRewardPoint())
                .reason(PointReason.QUEST_COMPLETION)
                .build());

        quest.changeStatus(QuestStatus.COMPLETED);
        return ParticipationResponse.from(participation);
    }

    @Transactional
    public ParticipationResponse reject(Long authorId, Long participationId, String rejectionReason) {
        QuestParticipation participation = getParticipation(participationId);
        if (!participation.getQuest().getAuthor().getId().equals(authorId)) {
            throw new ExpectedException("퀘스트 등록자만 완료 여부를 결정할 수 있습니다.", HttpStatus.FORBIDDEN);
        }
        try {
            participation.reject(rejectionReason, LocalDateTime.now());
        } catch (IllegalStateException e) {
            throw new ExpectedException(e.getMessage(), HttpStatus.CONFLICT);
        }
        return ParticipationResponse.from(participation);
    }

    public List<ParticipationResponse> getParticipants(Long authorId, Long questId) {
        Quest quest = getQuest(questId);
        if (!quest.getAuthor().getId().equals(authorId)) {
            throw new ExpectedException("퀘스트 등록자만 참여자 목록을 조회할 수 있습니다.", HttpStatus.FORBIDDEN);
        }
        return participationRepository.findByQuestId(questId).stream()
                .map(ParticipationResponse::from)
                .toList();
    }

    private Quest getQuest(Long questId) {
        return questRepository.findById(questId)
                .orElseThrow(() -> new ExpectedException("퀘스트를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ExpectedException("사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
    }

    private QuestParticipation getParticipation(Long participationId) {
        return participationRepository.findById(participationId)
                .orElseThrow(() -> new ExpectedException("참여 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
    }
}
