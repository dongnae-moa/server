package zaman.dongnaemoa.domain.quest.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import zaman.dongnaemoa.global.multipart.MultipartJsonParser;
import zaman.dongnaemoa.global.security.CustomUserDetails;

@Tag(name = "Quest", description = "퀘스트 등록/조회/삭제 API. 퀘스트는 사용자가 가입한 동네 단위로 등록되며, 등록자 본인만 삭제할 수 있다")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/v1/quests")
@RequiredArgsConstructor
public class QuestController {

    private final QuestService questService;
    private final MultipartJsonParser multipartJsonParser;

    @Operation(
            summary = "퀘스트 등록",
            description = "로그인한 사용자가 자신이 가입한 동네에 새 퀘스트를 등록한다. 등록 시 상태는 자동으로 RECRUITING이 된다. "
                    + "동네에 가입하지 않은 사용자는 등록할 수 없다. multipart/form-data로 요청하며, "
                    + "request 파트는 CreateQuestRequest를 직렬화한 JSON 문자열, image 파트는 이미지 파일(선택)이다. "
                    + "request 파트에 담을 JSON의 필드: "
                    + "title(문자열, 필수, 최대 100자), "
                    + "description(문자열, 선택), "
                    + "latitude(실수, 필수, 퀘스트가 진행되는 위치의 위도), "
                    + "longitude(실수, 필수, 퀘스트가 진행되는 위치의 경도). "
                    + "예상 소요시간(minutes), 보상 포인트(rewardPoint), 난이도(difficulty), 확인 목록(checkpoints)은 "
                    + "제목/설명을 바탕으로 AI가 자동으로 산정하므로 요청에 포함하지 않는다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "등록 성공"),
            @ApiResponse(responseCode = "400", description = "요청 값 검증 실패 또는 동네 미가입"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CommonApiResponse<QuestResponse> create(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Parameter(
                    description = "CreateQuestRequest를 직렬화한 JSON 문자열. 필드 구조는 아래 스키마 참고.",
                    schema = @Schema(type = "string",
                            example = "{\"title\":\"공원 쓰레기 치우기\",\"description\":\"OO공원 벤치 주변에 버려진 쓰레기를 "
                                    + "치우고 인증샷을 남겨주세요.\","
                                    + "\"latitude\":37.5665,\"longitude\":126.9780}"))
            @RequestPart("request") String requestJson,
            @Parameter(description = "퀘스트 이미지 파일 (선택)")
            @RequestPart(value = "image", required = false) MultipartFile image) {
        CreateQuestRequest request = multipartJsonParser.parse(requestJson, CreateQuestRequest.class);
        QuestResponse response = questService.create(principal.getUserId(), request, image);
        return CommonApiResponse.created("퀘스트가 등록되었습니다.", response);
    }

    @Operation(
            summary = "동네별 퀘스트 목록 조회",
            description = "특정 동네에 등록된 모든 퀘스트를 상태와 무관하게 조회한다. "
                    + "현재 위치(latitude, longitude)를 기준으로 각 퀘스트까지의 거리(미터)를 계산하여 "
                    + "가까운 순으로 정렬해 반환한다."
    )
    @GetMapping
    public CommonApiResponse<List<QuestResponse>> findByNeighborhood(
            @Parameter(description = "조회할 동네 ID", example = "1") @RequestParam Long neighborhoodId,
            @Parameter(description = "현재 위치 위도", example = "37.5665") @RequestParam Double latitude,
            @Parameter(description = "현재 위치 경도", example = "126.9780") @RequestParam Double longitude) {
        return CommonApiResponse.success("조회에 성공했습니다.",
                questService.findByNeighborhood(neighborhoodId, latitude, longitude));
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
    public CommonApiResponse<Void> delete(@AuthenticationPrincipal CustomUserDetails principal,
                                           @Parameter(description = "삭제할 퀘스트 ID") @PathVariable Long questId) {
        questService.delete(principal.getUserId(), questId);
        return CommonApiResponse.success("퀘스트가 삭제되었습니다.");
    }
}
