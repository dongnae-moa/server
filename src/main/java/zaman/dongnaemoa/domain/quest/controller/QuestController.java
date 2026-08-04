package zaman.dongnaemoa.domain.quest.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import team.themoment.sdk.response.CommonApiResponse;
import zaman.dongnaemoa.domain.quest.dto.CreateQuestRequest;
import zaman.dongnaemoa.domain.quest.dto.QuestResponse;
import zaman.dongnaemoa.domain.quest.service.QuestService;
import zaman.dongnaemoa.global.security.CustomUserDetails;

@Tag(name = "Quest", description = "퀘스트 등록/조회/삭제 API. 퀘스트는 사용자가 가입한 동네 단위로 등록되며, 등록자 본인만 삭제할 수 있다")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/v1/quests")
@RequiredArgsConstructor
public class QuestController {

    private final QuestService questService;

    @Operation(
            summary = "퀘스트 등록",
            description = "로그인한 사용자가 자신이 가입한 동네에 새 퀘스트를 등록한다. 등록 시 상태는 자동으로 RECRUITING이 된다. "
                    + "동네에 가입하지 않은 사용자는 등록할 수 없다. multipart/form-data로 요청하며, "
                    + "request 파트는 JSON(CreateQuestRequest), image 파트는 이미지 파일(선택)이다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "등록 성공"),
            @ApiResponse(responseCode = "400", description = "요청 값 검증 실패 또는 동네 미가입"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CommonApiResponse<QuestResponse> create(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestPart("request") CreateQuestRequest request,
            @Parameter(description = "퀘스트 이미지 파일 (선택)")
            @RequestPart(value = "image", required = false) MultipartFile image) {
        QuestResponse response = questService.create(principal.getUserId(), request, image);
        return CommonApiResponse.created("퀘스트가 등록되었습니다.", response);
    }

    @Operation(
            summary = "동네별 퀘스트 목록 조회",
            description = "특정 동네에 등록된 모든 퀘스트를 상태와 무관하게 조회한다."
    )
    @GetMapping
    public List<QuestResponse> findByNeighborhood(
            @Parameter(description = "조회할 동네 ID", example = "1") @RequestParam Long neighborhoodId) {
        return questService.findByNeighborhood(neighborhoodId);
    }

    @Operation(
            summary = "퀘스트 삭제",
            description = "퀘스트를 등록한 본인만 삭제할 수 있다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "본인이 등록한 퀘스트가 아님"),
            @ApiResponse(responseCode = "404", description = "퀘스트를 찾을 수 없음")
    })
    @DeleteMapping("/{questId}")
    public void delete(@AuthenticationPrincipal CustomUserDetails principal,
                        @Parameter(description = "삭제할 퀘스트 ID") @PathVariable Long questId) {
        questService.delete(principal.getUserId(), questId);
    }
}
