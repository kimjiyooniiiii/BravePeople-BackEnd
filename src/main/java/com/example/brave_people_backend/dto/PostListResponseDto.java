package com.example.brave_people_backend.dto;

import com.example.brave_people_backend.entity.Post;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * (GET /posts) 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostListResponseDto {

    private String hasNext;
    private List<PostListVo> data;

    public static PostListResponseDto of(boolean hasNext, List<PostListVo> data) {
        return PostListResponseDto.builder()
                .hasNext(hasNext ? "true" : "false")
                .data(data)
                .build();
    }

}
