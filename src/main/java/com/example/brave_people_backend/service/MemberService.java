package com.example.brave_people_backend.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.example.brave_people_backend.dto.LocationRequestDto;
import com.example.brave_people_backend.dto.LocationResponseDto;
import com.example.brave_people_backend.dto.ProfileResponseDto;
import com.example.brave_people_backend.entity.Member;
import com.example.brave_people_backend.repository.MemberRepository;
import com.example.brave_people_backend.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    //위치 정보 변경
    @Transactional
    public LocationResponseDto updateLocation(LocationRequestDto locationRequestDto) {

        //토큰으로 현재 회원 검색, 없으면 예외처리
        Member findMember = memberRepository.findById(SecurityUtil.getCurrentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "회원을 찾을 수 없습니다."));

        //위도, 경도 변경
        findMember.changeLatAndLng(new BigDecimal(locationRequestDto.getLat()), new BigDecimal(locationRequestDto.getLng()));

        return LocationResponseDto.of(findMember);
    }

    public ProfileResponseDto getProfileInfo(Long memberId) {
        return ProfileResponseDto.of(memberRepository.findById(memberId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "유효하지 않은 멤버ID"))
        );
    }

    // 프로필 이미지 변경
    @Transactional
    public String updateProfileImage(MultipartFile file) throws IOException {
        String originalFilename = "profile/" + file.getOriginalFilename();

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());

        amazonS3.putObject(bucket, originalFilename, file.getInputStream(), metadata);

        String imgUrl = amazonS3.getUrl(bucket, originalFilename).toString();



        return imgUrl;
    }
}
