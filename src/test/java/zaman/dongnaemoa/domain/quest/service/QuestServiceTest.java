package zaman.dongnaemoa.domain.quest.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import team.themoment.sdk.exception.ExpectedException;
import zaman.dongnaemoa.domain.neighborhood.entity.Neighborhood;
import zaman.dongnaemoa.domain.quest.dto.CreateQuestRequest;
import zaman.dongnaemoa.domain.quest.dto.QuestResponse;
import zaman.dongnaemoa.domain.quest.entity.Quest;
import zaman.dongnaemoa.domain.quest.repository.QuestRepository;
import zaman.dongnaemoa.domain.user.entity.User;
import zaman.dongnaemoa.domain.user.repository.UserRepository;
import zaman.dongnaemoa.global.storage.FileStorageService;

@ExtendWith(MockitoExtension.class)
class QuestServiceTest {

    @Mock
    private QuestRepository questRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FileStorageService fileStorageService;

    private QuestService questService;

    @BeforeEach
    void setUp() {
        questService = new QuestService(questRepository, userRepository, fileStorageService);
    }

    private Neighborhood neighborhood() {
        return Neighborhood.builder()
                .administrativeCode("code")
                .name("청운동")
                .sido("서울특별시")
                .sigungu("종로구")
                .latitude(BigDecimal.valueOf(37.58))
                .longitude(BigDecimal.valueOf(126.97))
                .build();
    }

    @Test
    @DisplayName("동네에 가입한 사용자는 이미지 없이 퀘스트를 등록할 수 있다")
    void create_withoutImage_success() {
        User author = User.builder().email("a@test.com").password("pw").nickname("nick")
                .neighborhood(neighborhood()).build();
        CreateQuestRequest request = new CreateQuestRequest("쓰레기 치우기", "설명", 500);
        Quest saved = Quest.builder().title(request.title()).description(request.description())
                .rewardPoint(request.rewardPoint()).author(author).neighborhood(author.getNeighborhood()).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(author));
        when(questRepository.save(any(Quest.class))).thenReturn(saved);

        QuestResponse response = questService.create(1L, request, null);

        assertThat(response.title()).isEqualTo("쓰레기 치우기");
        assertThat(response.imageUrl()).isNull();
        verify(fileStorageService, never()).store(any());
    }

    @Test
    @DisplayName("이미지가 첨부되면 저장 후 URL을 퀘스트에 반영한다")
    void create_withImage_storesFileAndSetsUrl() {
        User author = User.builder().email("a@test.com").password("pw").nickname("nick")
                .neighborhood(neighborhood()).build();
        CreateQuestRequest request = new CreateQuestRequest("쓰레기 치우기", "설명", 500);
        MultipartFile image = new MockMultipartFile("image", "photo.jpg", "image/jpeg", "content".getBytes());

        when(userRepository.findById(1L)).thenReturn(Optional.of(author));
        when(fileStorageService.store(image)).thenReturn("http://localhost:8080/files/abc.jpg");
        when(questRepository.save(any(Quest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        QuestResponse response = questService.create(1L, request, image);

        assertThat(response.imageUrl()).isEqualTo("http://localhost:8080/files/abc.jpg");
    }

    @Test
    @DisplayName("동네에 가입하지 않은 사용자는 퀘스트를 등록할 수 없다")
    void create_userWithoutNeighborhood_throwsBadRequest() {
        User author = User.builder().email("a@test.com").password("pw").nickname("nick").neighborhood(null).build();
        CreateQuestRequest request = new CreateQuestRequest("제목", "설명", 500);
        when(userRepository.findById(1L)).thenReturn(Optional.of(author));

        assertThatThrownBy(() -> questService.create(1L, request, null))
                .isInstanceOf(ExpectedException.class)
                .hasMessageContaining("동네에 가입");
    }

    @Test
    @DisplayName("존재하지 않는 사용자는 퀘스트를 등록할 수 없다")
    void create_userNotFound_throwsNotFound() {
        CreateQuestRequest request = new CreateQuestRequest("제목", "설명", 500);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> questService.create(1L, request, null))
                .isInstanceOf(ExpectedException.class)
                .hasMessageContaining("사용자를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("동네별 퀘스트 목록을 조회한다")
    void findByNeighborhood_returnsQuests() {
        User author = User.builder().email("a@test.com").password("pw").nickname("nick")
                .neighborhood(neighborhood()).build();
        Quest quest = Quest.builder().title("제목").rewardPoint(100).author(author)
                .neighborhood(author.getNeighborhood()).build();
        when(questRepository.findByNeighborhoodId(1L)).thenReturn(List.of(quest));

        List<QuestResponse> responses = questService.findByNeighborhood(1L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).title()).isEqualTo("제목");
    }

    @Test
    @DisplayName("등록자 본인은 퀘스트를 삭제할 수 있다")
    void delete_byAuthor_success() {
        User author = User.builder().email("a@test.com").password("pw").nickname("nick")
                .neighborhood(neighborhood()).build();
        ReflectionTestUtils.setField(author, "id", 1L);
        Quest quest = Quest.builder().title("제목").rewardPoint(100).author(author)
                .neighborhood(author.getNeighborhood()).build();
        when(questRepository.findById(10L)).thenReturn(Optional.of(quest));

        questService.delete(1L, 10L);

        verify(questRepository).delete(quest);
    }

    @Test
    @DisplayName("등록자가 아니면 삭제할 수 없다")
    void delete_notAuthor_throwsForbidden() {
        User author = User.builder().email("a@test.com").password("pw").nickname("nick")
                .neighborhood(neighborhood()).build();
        ReflectionTestUtils.setField(author, "id", 1L);
        Quest quest = Quest.builder().title("제목").rewardPoint(100).author(author)
                .neighborhood(author.getNeighborhood()).build();
        when(questRepository.findById(10L)).thenReturn(Optional.of(quest));

        assertThatThrownBy(() -> questService.delete(2L, 10L))
                .isInstanceOf(ExpectedException.class)
                .hasMessageContaining("본인이 등록한 퀘스트만");
    }

    @Test
    @DisplayName("존재하지 않는 퀘스트는 삭제할 수 없다")
    void delete_questNotFound_throwsNotFound() {
        when(questRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> questService.delete(1L, 10L))
                .isInstanceOf(ExpectedException.class)
                .hasMessageContaining("퀘스트를 찾을 수 없습니다");
    }
}
