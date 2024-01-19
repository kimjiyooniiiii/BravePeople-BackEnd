package com.example.brave_people_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class LoginResponseDto {

    private String lat;

    private String lng;

    private String nickname;

    private String memberId;

    private TokenDto tokenDto;
}
