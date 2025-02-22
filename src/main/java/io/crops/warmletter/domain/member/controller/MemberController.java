package io.crops.warmletter.domain.member.controller;

import io.crops.warmletter.domain.member.dto.response.ZipCodeResponse;
import io.crops.warmletter.domain.member.service.MemberService;
import io.crops.warmletter.global.response.BaseResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/zipCode")
    public ResponseEntity<BaseResponse<ZipCodeResponse>> createZipCode(HttpServletResponse response) {
        ZipCodeResponse zipCode = memberService.createZipCode(response);
        return ResponseEntity.ok(BaseResponse.of(zipCode, "우편번호 생성 완료"));
    }
}
