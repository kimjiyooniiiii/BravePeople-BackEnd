package com.example.brave_people_backend.member.dto;

import com.example.brave_people_backend.board.dto.PostListVo;
import com.example.brave_people_backend.entity.Member;
import lombok.Builder;
import lombok.Getter;

import java.util.List;


@Getter
@Builder
public class ProfileResponseDto {

    private String username;
    private String nickname;
    private String gender;
    private String introduction;
    private double score;
    private int medalCount;
    private String profileImage;
    private Long memberId;
    private List<PostListVo> postListVo;
    private List<ReviewListDto> reviews;

    public static ProfileResponseDto of(Member member, List<PostListVo> postListVo, double score, List<ReviewListDto> reviews) {
        return ProfileResponseDto.builder()
                .username(member.getUsername())
                .nickname(member.getNickname())
                .gender(member.isGender() ? "여성" : "남성")
                .introduction(member.getIntroduction())
                .score(score)
                .reviews(reviews)
                .medalCount(member.getMedalCount())
                .profileImage(member.getProfileImg())
                .memberId(member.getMemberId())
                .postListVo(postListVo)
                .build();
    }

}
