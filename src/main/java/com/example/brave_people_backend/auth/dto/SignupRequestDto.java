package com.example.brave_people_backend.auth.dto;

import com.example.brave_people_backend.entity.Member;
import com.example.brave_people_backend.enumclass.Authority;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequestDto {

    @NotBlank @Size(min = 2, max = 5)
    private String name;

    @NotBlank @Size(min = 2, max = 2)
    private String gender;

    @NotBlank @Size(min = 6, max = 20, message = "아이디 형식 오류")
    private String username;

    @NotBlank  @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*[0-9]).{8,}$", message = "비밀번호 형식 오류") // 영문 + 숫자, 8자리 아상
    private String pw;

    @NotBlank @Size(max = 30) @Email
    private String email;

    @NotBlank @Size(max = 6)
    private String nickname;

    private String lat;

    private String lng;

    private Long emailId;

    public Member toMember(PasswordEncoder passwordEncoder) {
        return Member.builder()
                .username(username)
                .pw(passwordEncoder.encode(pw))
                .email(email)
                .nickname(nickname)
                .gender(gender.equals("여성"))
                .lat(new BigDecimal(lat == null ? "37.566815192" : lat))
                .lng(new BigDecimal(lng == null ? "126.97864095" : lng))
                .authority(Authority.ROLE_USER)
                .name(name)
                .build();
    }

}