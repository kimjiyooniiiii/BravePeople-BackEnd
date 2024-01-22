package com.example.brave_people_backend.controller;

import com.example.brave_people_backend.dto.*;
import com.example.brave_people_backend.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {

    private final MemberService memberService;

    //위치 변경
    @PatchMapping("/location")
    public ResponseEntity<LocationResponseDto> updateLocation(@RequestBody LocationRequestDto locationRequestDto) {
        return ResponseEntity.ok(memberService.updateLocation(locationRequestDto));
    }

    // 프로필 페이지
    @GetMapping("/profile")
    public ProfileResponseDto getProfileInfo(@RequestParam("memberid") Long memberId) {
        return memberService.getProfileInfo(memberId);
    }

    // 프로필 이미지 변경
    @PatchMapping("/profile/image")
    public ProfileImageResponseDto updateProfileImage(@RequestPart("file") MultipartFile file) throws IOException {
        return memberService.updateProfileImage(file);
    }

    //닉네임, 자기소개 수정
    @PatchMapping("/profile")
    public UpdateProfileInfoResponseDto updateProfileInfo(@RequestBody UpdateProfileInfoRequestDto updateProfileInfoRequestDto) {
        return memberService.updateProfileInfo(updateProfileInfoRequestDto);
    }

    //비밀번호 인증
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/pw")
    public void reconfirmPassword(@RequestBody PWReconfirmRequestDto pwReconfirmRequestDto){
        memberService.reconfirmPassword(pwReconfirmRequestDto);
    }

    //비밀번호 재설정
    @PatchMapping("/pw")
    public void updatePassword(@RequestBody @Valid UpdatePwRequestDto updatePwRequestDto) {
        memberService.updatePassword(updatePwRequestDto);
    }

    // 로그아웃
    @PostMapping("/logout")
    public void logout() {
        memberService.logout();
    }
}
