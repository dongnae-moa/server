package zaman.dongnaemoa.domain.neighborhood.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import team.themoment.sdk.response.CommonApiResponse;
import zaman.dongnaemoa.domain.neighborhood.dto.JoinNeighborhoodRequest;
import zaman.dongnaemoa.domain.neighborhood.dto.NeighborhoodResponse;
import zaman.dongnaemoa.domain.neighborhood.service.NeighborhoodService;
import zaman.dongnaemoa.global.security.CustomUserDetails;

@Tag(name = "Neighborhood", description = "동네 가입 API. 동네는 미리 관리자가 등록해둔 데이터이며, 사용자는 GPS 좌표로 자신이 속한 동네를 감지/가입한다")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/v1/neighborhoods")
@RequiredArgsConstructor
public class NeighborhoodController {

    private final NeighborhoodService neighborhoodService;

    @Operation(
            summary = "동네 가입 (GPS 기반)",
            description = "현재 위/경도를 보내면 서버가 등록된 동네 중 가장 가까운 동네(직선거리 기준)를 찾아 로그인한 사용자를 그 동네로 이동시킨다. "
                    + "동네 이동은 언제든 재호출로 가능하며, 매번 마지막으로 가입한 동네만 유지된다(1인 1동네)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "가입(이동) 성공, 매칭된 동네 정보 반환"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없거나 등록된 동네 데이터가 없음")
    })
    @PostMapping("/join")
    public CommonApiResponse<NeighborhoodResponse> join(@AuthenticationPrincipal CustomUserDetails principal,
                                                          @Valid @RequestBody JoinNeighborhoodRequest request) {
        NeighborhoodResponse response = neighborhoodService.join(principal.getUserId(), request);
        return CommonApiResponse.success("동네 가입에 성공했습니다.", response);
    }

    @Operation(
            summary = "동네 목록 조회",
            description = "관리자가 미리 등록해둔 모든 동네 목록을 조회한다. 퀘스트 목록 조회(GET /v1/quests) 시 필요한 "
                    + "neighborhoodId와 그 동네 이름/지역 정보를 매핑하기 위해 프론트엔드에서 사용한다."
    )
    @GetMapping
    public CommonApiResponse<List<NeighborhoodResponse>> findAll() {
        return CommonApiResponse.success("조회에 성공했습니다.", neighborhoodService.findAll());
    }

    @Operation(
            summary = "동네 단건 조회",
            description = "동네 ID로 해당 동네의 이름/지역 정보를 조회한다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "동네를 찾을 수 없음")
    })
    @GetMapping("/{neighborhoodId}")
    public CommonApiResponse<NeighborhoodResponse> findById(
            @Parameter(description = "조회할 동네 ID", example = "1") @PathVariable Long neighborhoodId) {
        return CommonApiResponse.success("조회에 성공했습니다.", neighborhoodService.findById(neighborhoodId));
    }
}
