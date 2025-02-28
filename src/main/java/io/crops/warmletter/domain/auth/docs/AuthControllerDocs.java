package io.crops.warmletter.domain.auth.docs;

import io.crops.warmletter.domain.auth.dto.TokenResponse;
import io.crops.warmletter.domain.auth.dto.TokenStorageResponse;
import io.crops.warmletter.global.error.response.ErrorResponse;
import io.crops.warmletter.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "인증 관련 API", description = "토큰 재발급, 로그아웃, 임시 저장소 토큰 조회 기능의 API를 제공합니다.")
public interface AuthControllerDocs {

    @Operation(summary = "토큰 재발급",
            description = "만료된 액세스 토큰을 대체할 새로운 액세스 토큰과 리프레시 토큰을 재발급합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "토큰 재발급 완료"),
                    @ApiResponse(responseCode = "401", description = "리프레시 토큰이 유효하지 않음",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            })
    @PostMapping("/reissue")
    ResponseEntity<BaseResponse<TokenResponse>> reissue(
            @RequestHeader("Authorization") String bearerToken,
            @CookieValue(name = "refresh_token") String refreshToken,
            HttpServletResponse response
    );

    @Operation(summary = "로그아웃",
            description = "만료된 액세스 토큰을 대체할 새로운 액세스 토큰과 리프레시 토큰을 재발급합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "로그아웃 완료"),
                    @ApiResponse(responseCode = "401", description = "리프레시 토큰이 유효하지 않음",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            })
    @PostMapping("/logout")
    ResponseEntity<Void> logout(
            @RequestHeader("Authorization") String bearerToken,
            @CookieValue(value = "refresh_token", required = false) String refreshToken,
            HttpServletResponse response) ;

    @Operation(summary = "임시 저장소 토큰 조회",
            description = "소셜 로그인 후 액세스 토큰과 리프레시 토큰을 전달합니다",
            responses = {
                    @ApiResponse(responseCode = "200", description = "임시 저장소 토큰 조회 완료"),
                    @ApiResponse(responseCode = "401", description = "임시 저장소에 토큰 UUID 값이 유효하지 않음",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            })
    @GetMapping("/auth/token")
    ResponseEntity<BaseResponse<TokenStorageResponse>> getToken(@RequestParam("state") String stateToken);
}
