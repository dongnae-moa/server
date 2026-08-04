package zaman.dongnaemoa.domain.participation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import team.themoment.sdk.exception.ExpectedException;
import zaman.dongnaemoa.domain.participation.dto.ParticipationResponse;
import zaman.dongnaemoa.domain.participation.dto.SubmitProofRequest;
import zaman.dongnaemoa.domain.participation.entity.ParticipationStatus;
import zaman.dongnaemoa.domain.participation.entity.QuestParticipation;
import zaman.dongnaemoa.domain.participation.repository.QuestParticipationRepository;
import zaman.dongnaemoa.domain.quest.entity.Quest;
import zaman.dongnaemoa.domain.quest.entity.QuestStatus;
import zaman.dongnaemoa.domain.quest.repository.QuestRepository;
import zaman.dongnaemoa.domain.reward.repository.PointHistoryRepository;
import zaman.dongnaemoa.domain.user.entity.User;
import zaman.dongnaemoa.domain.user.repository.UserRepository;
import zaman.dongnaemoa.global.storage.FileStorageService;

@ExtendWith(MockitoExtension.class)
class ParticipationServiceTest {

    @Mock
    private QuestParticipationRepository participationRepository;

    @Mock
    private QuestRepository questRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PointHistoryRepository pointHistoryRepository;

    @Mock
    private FileStorageService fileStorageService;

    private ParticipationService participationService;

    @BeforeEach
    void setUp() {
        participationService = new ParticipationService(
                participationRepository, questRepository, userRepository, pointHistoryRepository, fileStorageService);
    }

    private User user(long id, String email) {
        User user = User.builder().email(email).password("pw").nickname("nick-" + id).neighborhood(null).build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Quest quest(User author, int rewardPoint) {
        Quest quest = Quest.builder().title("제목").rewardPoint(rewardPoint).author(author).neighborhood(null).build();
        ReflectionTestUtils.setField(quest, "id", 100L);
        return quest;
    }

    @Test
    @DisplayName("타인이 등록한 퀘스트에 참여할 수 있다")
    void join_success() {
        User author = user(1L, "author@test.com");
        User participant = user(2L, "participant@test.com");
        Quest quest = quest(author, 500);

        when(questRepository.findById(100L)).thenReturn(Optional.of(quest));
        when(userRepository.findById(2L)).thenReturn(Optional.of(participant));
        when(participationRepository.findByQuestIdAndParticipantId(100L, 2L)).thenReturn(Optional.empty());
        when(participationRepository.save(any(QuestParticipation.class))).thenAnswer(inv -> inv.getArgument(0));

        ParticipationResponse response = participationService.join(2L, 100L);

        assertThat(response.status()).isEqualTo(ParticipationStatus.JOINED);
        assertThat(response.participantId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("본인이 등록한 퀘스트에는 참여할 수 없다")
    void join_ownQuest_throwsBadRequest() {
        User author = user(1L, "author@test.com");
        Quest quest = quest(author, 500);
        when(questRepository.findById(100L)).thenReturn(Optional.of(quest));
        when(userRepository.findById(1L)).thenReturn(Optional.of(author));

        assertThatThrownBy(() -> participationService.join(1L, 100L))
                .isInstanceOf(ExpectedException.class)
                .hasMessageContaining("본인이 등록한 퀘스트");
    }

    @Test
    @DisplayName("이미 참여한 퀘스트는 중복 참여할 수 없다")
    void join_alreadyJoined_throwsConflict() {
        User author = user(1L, "author@test.com");
        User participant = user(2L, "participant@test.com");
        Quest quest = quest(author, 500);
        QuestParticipation existing = QuestParticipation.builder().quest(quest).participant(participant).build();

        when(questRepository.findById(100L)).thenReturn(Optional.of(quest));
        when(userRepository.findById(2L)).thenReturn(Optional.of(participant));
        when(participationRepository.findByQuestIdAndParticipantId(100L, 2L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> participationService.join(2L, 100L))
                .isInstanceOf(ExpectedException.class)
                .hasMessageContaining("이미 참여한 퀘스트");
    }

    @Test
    @DisplayName("모집 중이 아닌 퀘스트에는 참여할 수 없다")
    void join_questNotRecruiting_throwsConflict() {
        User author = user(1L, "author@test.com");
        User participant = user(2L, "participant@test.com");
        Quest quest = quest(author, 500);
        quest.changeStatus(QuestStatus.COMPLETED);

        when(questRepository.findById(100L)).thenReturn(Optional.of(quest));
        when(userRepository.findById(2L)).thenReturn(Optional.of(participant));

        assertThatThrownBy(() -> participationService.join(2L, 100L))
                .isInstanceOf(ExpectedException.class)
                .hasMessageContaining("모집 중인 퀘스트");
    }

    @Test
    @DisplayName("본인의 참여 건에 인증(이미지 없이)을 제출한다")
    void submitProof_withoutImage_success() {
        User author = user(1L, "author@test.com");
        User participant = user(2L, "participant@test.com");
        Quest quest = quest(author, 500);
        QuestParticipation participation = QuestParticipation.builder().quest(quest).participant(participant).build();
        when(participationRepository.findById(5L)).thenReturn(Optional.of(participation));

        ParticipationResponse response = participationService.submitProof(
                2L, 5L, new SubmitProofRequest("완료했습니다."), null);

        assertThat(response.status()).isEqualTo(ParticipationStatus.SUBMITTED);
        assertThat(response.proofImageUrl()).isNull();
        verify(fileStorageService, never()).store(any());
    }

    @Test
    @DisplayName("인증 이미지가 첨부되면 저장 후 URL을 참여 정보에 반영한다")
    void submitProof_withImage_storesFileAndSetsUrl() {
        User author = user(1L, "author@test.com");
        User participant = user(2L, "participant@test.com");
        Quest quest = quest(author, 500);
        QuestParticipation participation = QuestParticipation.builder().quest(quest).participant(participant).build();
        MultipartFile image = new MockMultipartFile("image", "proof.jpg", "image/jpeg", "content".getBytes());

        when(participationRepository.findById(5L)).thenReturn(Optional.of(participation));
        when(fileStorageService.store(image)).thenReturn("http://localhost:8080/files/proof.jpg");

        ParticipationResponse response = participationService.submitProof(
                2L, 5L, new SubmitProofRequest("완료했습니다."), image);

        assertThat(response.proofImageUrl()).isEqualTo("http://localhost:8080/files/proof.jpg");
    }

    @Test
    @DisplayName("본인의 참여 건이 아니면 인증을 제출할 수 없다")
    void submitProof_notOwnParticipation_throwsForbidden() {
        User author = user(1L, "author@test.com");
        User participant = user(2L, "participant@test.com");
        Quest quest = quest(author, 500);
        QuestParticipation participation = QuestParticipation.builder().quest(quest).participant(participant).build();
        when(participationRepository.findById(5L)).thenReturn(Optional.of(participation));

        assertThatThrownBy(() -> participationService.submitProof(
                999L, 5L, new SubmitProofRequest("완료했습니다."), null))
                .isInstanceOf(ExpectedException.class)
                .hasMessageContaining("본인의 참여 건만");
    }

    @Test
    @DisplayName("이미 제출된 참여 건은 다시 인증을 제출할 수 없다")
    void submitProof_alreadySubmitted_throwsConflict() {
        User author = user(1L, "author@test.com");
        User participant = user(2L, "participant@test.com");
        Quest quest = quest(author, 500);
        QuestParticipation participation = QuestParticipation.builder().quest(quest).participant(participant).build();
        participation.submitProof(null, "먼저 제출", java.time.LocalDateTime.now());
        when(participationRepository.findById(5L)).thenReturn(Optional.of(participation));

        assertThatThrownBy(() -> participationService.submitProof(
                2L, 5L, new SubmitProofRequest("다시 제출"), null))
                .isInstanceOf(ExpectedException.class);
    }

    @Test
    @DisplayName("등록자가 승인하면 참여자에게 포인트가 지급되고 퀘스트가 완료 처리된다")
    void approve_success_grantsPointAndCompletesQuest() {
        User author = user(1L, "author@test.com");
        User participant = user(2L, "participant@test.com");
        Quest quest = quest(author, 500);
        QuestParticipation participation = QuestParticipation.builder().quest(quest).participant(participant).build();
        participation.submitProof(null, "인증합니다", java.time.LocalDateTime.now());
        when(participationRepository.findById(5L)).thenReturn(Optional.of(participation));

        ParticipationResponse response = participationService.approve(1L, 5L);

        assertThat(response.status()).isEqualTo(ParticipationStatus.APPROVED);
        assertThat(participant.getPoint()).isEqualTo(500);
        assertThat(quest.getStatus()).isEqualTo(QuestStatus.COMPLETED);
        verify(pointHistoryRepository).save(any());
    }

    @Test
    @DisplayName("등록자가 아니면 승인할 수 없다")
    void approve_notAuthor_throwsForbidden() {
        User author = user(1L, "author@test.com");
        User participant = user(2L, "participant@test.com");
        Quest quest = quest(author, 500);
        QuestParticipation participation = QuestParticipation.builder().quest(quest).participant(participant).build();
        when(participationRepository.findById(5L)).thenReturn(Optional.of(participation));

        assertThatThrownBy(() -> participationService.approve(999L, 5L))
                .isInstanceOf(ExpectedException.class)
                .hasMessageContaining("퀘스트 등록자만");
    }

    @Test
    @DisplayName("등록자가 반려하면 사유가 기록되고 포인트는 지급되지 않는다")
    void reject_success_setsReasonWithoutPoint() {
        User author = user(1L, "author@test.com");
        User participant = user(2L, "participant@test.com");
        Quest quest = quest(author, 500);
        QuestParticipation participation = QuestParticipation.builder().quest(quest).participant(participant).build();
        participation.submitProof(null, "인증합니다", java.time.LocalDateTime.now());
        when(participationRepository.findById(5L)).thenReturn(Optional.of(participation));

        ParticipationResponse response = participationService.reject(1L, 5L, "인증 불충분");

        assertThat(response.status()).isEqualTo(ParticipationStatus.REJECTED);
        assertThat(response.rejectionReason()).isEqualTo("인증 불충분");
        assertThat(participant.getPoint()).isZero();
        verify(pointHistoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("이미 승인된 참여 건은 다시 승인할 수 없다")
    void approve_alreadyApproved_throwsConflict() {
        User author = user(1L, "author@test.com");
        User participant = user(2L, "participant@test.com");
        Quest quest = quest(author, 500);
        QuestParticipation participation = QuestParticipation.builder().quest(quest).participant(participant).build();
        participation.submitProof(null, "인증합니다", java.time.LocalDateTime.now());
        participation.approve(java.time.LocalDateTime.now());
        when(participationRepository.findById(5L)).thenReturn(Optional.of(participation));

        assertThatThrownBy(() -> participationService.approve(1L, 5L))
                .isInstanceOf(ExpectedException.class);
        verify(pointHistoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("존재하지 않는 참여 건은 승인/반려할 수 없다")
    void approve_participationNotFound_throwsNotFound() {
        when(participationRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> participationService.approve(1L, 5L))
                .isInstanceOf(ExpectedException.class)
                .hasMessageContaining("참여 정보를 찾을 수 없습니다");
    }
}
