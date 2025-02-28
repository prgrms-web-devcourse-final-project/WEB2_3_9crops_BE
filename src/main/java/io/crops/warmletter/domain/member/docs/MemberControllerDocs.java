package io.crops.warmletter.domain.member.docs;

import io.crops.warmletter.domain.member.dto.response.MeResponse;
import io.crops.warmletter.domain.member.dto.response.ZipCodeResponse;
import io.crops.warmletter.global.error.response.ErrorResponse;
import io.crops.warmletter.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

public interface MemberControllerDocs {

    @Operation(summary = "우편번호 발급",
            description = "우편번호가 발급되지 않은 회원에게 최초 1회 우편번호를 발급합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "우편번호 생성 완료"),
                    @ApiResponse(responseCode = "403", description = "우편번호가 이미 존재함",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "404", description = "유저를 찾을 수 없음",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            })
    @PostMapping("/zipCode")
    ResponseEntity<BaseResponse<ZipCodeResponse>> createZipCode(HttpServletResponse response);

    @Operation(summary = "마이페이지 조회",
            description = "로그인한 사용자의 정보를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "마이페이지 조회 완료"),
                    @ApiResponse(responseCode = "404", description = "유저를 찾을 수 없음",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            })
    @GetMapping("/me")
    ResponseEntity<BaseResponse<MeResponse>> getMe();

    @Operation(summary = "회원탈퇴",
            description = "로그인한 사용자가 회원 탈퇴를합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "회원 탈퇴 완료"),
                    @ApiResponse(responseCode = "403", description = "이미 탈퇴된 회원",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "404", description = "유저를 찾을 수 없음",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            })
    @DeleteMapping("/me")
    ResponseEntity<BaseResponse<Void>> deleteMe(
            @RequestHeader("Authorization") String bearerToken,
            @CookieValue(value = "refresh_token", required = false) String refreshToken,
            HttpServletResponse response
    );
}
