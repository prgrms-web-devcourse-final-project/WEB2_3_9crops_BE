package io.crops.warmletter.domain.member.controller;

import io.crops.warmletter.domain.member.docs.MemberControllerDocs;
import io.crops.warmletter.domain.member.dto.response.MeResponse;
import io.crops.warmletter.domain.member.dto.response.ZipCodeResponse;
import io.crops.warmletter.domain.member.service.MemberService;
import io.crops.warmletter.global.response.BaseResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController implements MemberControllerDocs {

    private final MemberService memberService;

    @PostMapping("/zipCode")
    public ResponseEntity<BaseResponse<ZipCodeResponse>> createZipCode(HttpServletResponse response) {
        ZipCodeResponse zipCode = memberService.createZipCode(response);
        return ResponseEntity.ok(BaseResponse.of(zipCode, "우편번호 생성 완료"));
    }

    @GetMapping("/me")
    public ResponseEntity<BaseResponse<MeResponse>> getMe() {
        MeResponse response = memberService.getMe();
        return ResponseEntity.ok(BaseResponse.of(response, "마이페이지 조회 완료"));
    }

    @DeleteMapping("/me")
    public ResponseEntity<BaseResponse<Void>> deleteMe(
            @RequestHeader("Authorization") String bearerToken,
            @CookieValue(value = "refresh_token", required = false) String refreshToken,
            HttpServletResponse response
    ) {
        memberService.deleteMe(bearerToken.substring(7), refreshToken, response);

        return ResponseEntity.ok(BaseResponse.of(null, "회원 탈퇴 완료"));
    }
}
