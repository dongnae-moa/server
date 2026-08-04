package zaman.dongnaemoa.domain.participation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import team.themoment.sdk.response.CommonApiResponse;
import zaman.dongnaemoa.domain.participation.dto.ParticipationResponse;
import zaman.dongnaemoa.domain.participation.dto.RejectParticipationRequest;
import zaman.dongnaemoa.domain.participation.dto.SubmitProofRequest;
import zaman.dongnaemoa.domain.participation.service.ParticipationService;
import zaman.dongnaemoa.global.security.CustomUserDetails;

@Tag(name = "Participation", description = "퀘스트 참여/인증/완료 승인 API. 참여 흐름: 참여(JOINED) → 인증 제출(SUBMITTED) → "
        + "등록자의 승인(APPROVED, 포인트 지급) 또는 반려(REJECTED)")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class ParticipationController {

    private final ParticipationService participationService;

    @Operation(
            summary = "퀘스트 참여",
            description = "로그인한 사용자가 퀘스트에 참여 신청한다. 본인이 등록한 퀘스트에는 참여할 수 없고, "
                    + "동일 퀘스트에 중복 참여할 수 없다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "참여 성공 (상태: JOINED)"),
            @ApiResponse(responseCode = "400", description = "본인이 등록한 퀘스트에 참여 시도"),
            @ApiResponse(responseCode = "404", description = "퀘스트 또는 사용자를 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "이미 참여한 퀘스트")
    })
    @PostMapping("/quests/{questId}/participations")
    public CommonApiResponse<ParticipationResponse> join(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Parameter(description = "참여할 퀘스트 ID") @PathVariable Long questId) {
        ParticipationResponse response = participationService.join(principal.getUserId(), questId);
        return CommonApiResponse.success("퀘스트 참여에 성공했습니다.", response);
    }

    @Operation(
            summary = "퀘스트 완료 인증 제출",
            description = "참여자 본인이 퀘스트 수행 완료 인증(이미지/설명)을 제출한다. 제출 시 상태가 SUBMITTED로 바뀌고 "
                    + "퀘스트 등록자의 승인/반려를 기다리게 된다. multipart/form-data로 요청하며, "
                    + "request 파트는 JSON(SubmitProofRequest), image 파트는 인증 이미지 파일(선택)이다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "제출 성공 (상태: SUBMITTED)"),
            @ApiResponse(responseCode = "403", description = "본인의 참여 건이 아님"),
            @ApiResponse(responseCode = "404", description = "참여 정보를 찾을 수 없음")
    })
    @PostMapping(value = "/participations/{participationId}/proof", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CommonApiResponse<ParticipationResponse> submitProof(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Parameter(description = "참여 ID") @PathVariable Long participationId,
            @RequestPart("request") SubmitProofRequest request,
            @Parameter(description = "인증 이미지 파일 (선택)")
            @RequestPart(value = "image", required = false) MultipartFile image) {
        ParticipationResponse response =
                participationService.submitProof(principal.getUserId(), participationId, request, image);
        return CommonApiResponse.success("인증 제출에 성공했습니다.", response);
    }

    @Operation(
            summary = "퀘스트 완료 승인",
            description = "퀘스트 등록자가 제출된 인증을 확인하고 승인한다. 승인 시 참여자에게 퀘스트의 rewardPoint만큼 "
                    + "포인트가 즉시 지급되고(User.point 증가 + PointHistory 기록), 퀘스트 상태가 COMPLETED로 바뀐다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "승인 성공, 포인트 지급 완료"),
            @ApiResponse(responseCode = "403", description = "퀘스트 등록자 본인이 아님"),
            @ApiResponse(responseCode = "404", description = "참여 정보를 찾을 수 없음")
    })
    @PostMapping("/participations/{participationId}/approve")
    public CommonApiResponse<ParticipationResponse> approve(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Parameter(description = "참여 ID") @PathVariable Long participationId) {
        ParticipationResponse response = participationService.approve(principal.getUserId(), participationId);
        return CommonApiResponse.success("승인에 성공했습니다. 참여자에게 포인트가 지급되었습니다.", response);
    }

    @Operation(
            summary = "퀘스트 완료 반려",
            description = "퀘스트 등록자가 제출된 인증이 부적절하다고 판단할 경우 사유와 함께 반려한다. 포인트는 지급되지 않는다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "반려 처리 성공 (상태: REJECTED)"),
            @ApiResponse(responseCode = "403", description = "퀘스트 등록자 본인이 아님"),
            @ApiResponse(responseCode = "404", description = "참여 정보를 찾을 수 없음")
    })
    @PostMapping("/participations/{participationId}/reject")
    public CommonApiResponse<ParticipationResponse> reject(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Parameter(description = "참여 ID") @PathVariable Long participationId,
            @Valid @RequestBody RejectParticipationRequest request) {
        ParticipationResponse response =
                participationService.reject(principal.getUserId(), participationId, request.rejectionReason());
        return CommonApiResponse.success("반려 처리되었습니다.", response);
    }
}
