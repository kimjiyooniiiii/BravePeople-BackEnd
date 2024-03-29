package com.example.brave_people_backend.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsernameResponseDto {

    private String username;

    // UsernameResponseDto 객체를 생성하는 함수 (of 함수를 이용함으로서 유지보수 편리)
    public static UsernameResponseDto of(String name) {
        return UsernameResponseDto.builder()
                .username(name)
                .build();
    }
}
