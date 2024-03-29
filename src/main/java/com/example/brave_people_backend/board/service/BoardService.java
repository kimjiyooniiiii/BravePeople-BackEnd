package com.example.brave_people_backend.board.service;

import com.example.brave_people_backend.board.dto.PostListResponseDto;
import com.example.brave_people_backend.board.dto.PostListVo;
import com.example.brave_people_backend.board.dto.PostRequestDto;
import com.example.brave_people_backend.board.dto.PostResponseDto;
import com.example.brave_people_backend.entity.Contact;
import com.example.brave_people_backend.entity.Member;
import com.example.brave_people_backend.entity.Post;
import com.example.brave_people_backend.entity.Review;
import com.example.brave_people_backend.enumclass.Act;
import com.example.brave_people_backend.enumclass.ContactStatus;
import com.example.brave_people_backend.exception.Custom404Exception;
import com.example.brave_people_backend.exception.CustomException;
import com.example.brave_people_backend.repository.BoardRepository;
import com.example.brave_people_backend.repository.ContactRepository;
import com.example.brave_people_backend.repository.MemberRepository;
import com.example.brave_people_backend.repository.ReviewRepository;
import com.example.brave_people_backend.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BoardService {

    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;
    private final ContactRepository contactRepository;
    private final ReviewRepository reviewRepository;

    // 글 목록 불러오기
    @Transactional(readOnly = true)
    public PostListResponseDto getPostList(String type, Integer distance, Integer page, Integer amount) {

        Long currentMemberId = SecurityUtil.getCurrentId(); //현재 로그인 한 MemberId 반환
        Sort sort = Sort.by(Sort.Direction.DESC, "postId"); //POST 테이블의 post_id 기준 내림차순 정렬 설정
        PageRequest pageRequest = PageRequest.of(page, amount, sort); //출력할 page와 amount 및 sort 기준 설정 (pageable 구현체)

        Slice<Post> postList;

        //비로그인 회원 OR distance 파라미터가 null OR distance 파라미터 값이 0일 경우
        if (currentMemberId == null || distance == null || distance == 0) {
            //Repository에 act(원정대/의뢰인), PageRequest 정보를 넘기고
            //검색된 Post 엔티티를 Slice<Post>로 반환 받음
            postList = boardRepository.findPostList(Act.valueOf(type), pageRequest);
        }
        //로그인한 회원일 경우
        else {
            //currentMemberId로 현재 로그인 한 Member 엔티티를 반환
            Member findMember = memberRepository.findById(currentMemberId)
                    .orElseThrow(() -> new CustomException(String.valueOf(currentMemberId), "존재하지 않는 멤버ID"));

            //Member 엔티티에서 위도 경도 데이터 반환
            BigDecimal lat = findMember.getLat();
            BigDecimal lng = findMember.getLng();

            //Repository에 act(원정대/의뢰인), distance(반경), lat(위도), lng(경도), PageRequest 정보를 넘기고
            //검색된 Post 엔티티를 Slice<Post>로 반환 받음
            postList = boardRepository.findPostListByRadius(Act.valueOf(type), distance, lat, lng, pageRequest);
        }

        return PostListResponseDto.of(postList.hasNext(), postList.map(PostListVo::of).toList());

    }

    //글 작성
    public void createPost(PostRequestDto postRequestDto) {
        Long currentMemberId = SecurityUtil.getCurrentId();

        //토큰으로 현재 회원 검색, 없으면 예외처리
        Member findMember = memberRepository.findById(currentMemberId)
                .orElseThrow(() -> new CustomException(String.valueOf(currentMemberId), "존재하지 않는 멤버ID"));

        //게시글 저장
        boardRepository.save(postRequestDto.toPost(findMember));
    }

    @Transactional(readOnly = true)
    public PostResponseDto getPost(Long postId) {
        Post findPost = boardRepository.findById(postId).
                orElseThrow(() -> new Custom404Exception(String.valueOf(postId), "존재하지 않는 게시글"));

        if (findPost.isDeleted()) {
            throw new Custom404Exception(String.valueOf(postId), "존재하지 않는 게시글");
        }

        return PostResponseDto.of(findPost, calcReviewScoreAvg(findPost.getMember()));
    }

    private double calcReviewScoreAvg(Member member) {
        // 리뷰들의 점수 평균을 구하고, 리뷰가 하나도 없을 시 0.0을 반환한다.
        double avg = reviewRepository.findByMember(member)
                .stream()
                .mapToInt(Review::getScore)
                .average()
                .orElse(0.0);
        return Math.round(avg*10)/10.0; //소수점 둘째 자리에서 반올림하여 반환
    }

    //글 수정
    public void updatePost(Long postId, PostRequestDto postRequestDto) {

        Post findPost = getFindPost(postId);
        findPost.updatePost(postRequestDto);
    }

    //글 삭제
    public void deletePost(Long postId) {

        //pathvariable에서 받은 postId로 post 객체 검색 -> 삭제되거나 비활성화된 게시글이면 오류
        Post findPost = getFindPost(postId);

        //해당 게시글에서 진행중인 의뢰가 있으면 삭제하지 못하게 함
        List<Contact> findContact = contactRepository.findContactOneByStatusAndPost(findPost);
        if (!findContact.isEmpty()) {
            throw new CustomException(String.valueOf(findPost.getPostId()), "진행중인 의뢰 존재");
        }

        //현재 Post와 관련된 의뢰 취소하기
        List<Contact> contacts = contactRepository.findContactsByPost(findPost);
        for (Contact contact : contacts) {
            contact.changeStatus("writer", ContactStatus.취소);
            contact.changeStatus("other", ContactStatus.취소);
        }

        findPost.onDeleted();
    }

    //pathvariable에서 받은 postId로 post 객체 검색 -> 삭제되거나 비활성화된 게시글이면 오류
    private Post getFindPost(Long postId) {
        Post findPost = boardRepository.findByPostId(postId)
                .orElseThrow(() -> new Custom404Exception(String.valueOf(postId), "존재하지 않는 게시글ID"));
        if (findPost.isDeleted()) {
            throw new CustomException(String.valueOf(postId), "삭제된 게시글");
        }
        else if(findPost.isDisabled()) {
            throw new CustomException(String.valueOf(postId), "비활성화된 게시글");
        }

        //현재 Post의 작성자와 다르면
        if (!findPost.getMember().getMemberId().equals(SecurityUtil.getCurrentId())) {
            throw new CustomException(String.valueOf(postId), "권한 없음");
        }
        return findPost;
    }

}
