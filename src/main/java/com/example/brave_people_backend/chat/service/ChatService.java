package com.example.brave_people_backend.chat.service;

import com.example.brave_people_backend.chat.dto.SendRequestDto;
import com.example.brave_people_backend.chat.dto.SendResponseDto;
import com.example.brave_people_backend.entity.Chat;
import com.example.brave_people_backend.entity.ChatRoom;
import com.example.brave_people_backend.entity.Member;
import com.example.brave_people_backend.enumclass.MessageType;
import com.example.brave_people_backend.enumclass.NotificationType;
import com.example.brave_people_backend.exception.CustomException;
import com.example.brave_people_backend.repository.ChatRepository;
import com.example.brave_people_backend.repository.ChatRoomRepository;
import com.example.brave_people_backend.repository.MemberRepository;
import com.example.brave_people_backend.sse.service.SseService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class ChatService {

    private final SimpMessageSendingOperations template;
    private final ChatRepository chatRepository;
    private final SseService sseService;
    private final ChatRoomRepository chatRoomRepository;
    private final MemberRepository memberRepository;

    // 메시지 전송
    public void sendMessage(SendRequestDto sendRequestDto, Long roomId, SimpMessageHeaderAccessor headerAccessor) {
        MessageType type = sendRequestDto.getType();

        //roomId로 채팅방을 찾음
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(
                () -> new CustomException(String.valueOf(roomId), "존재하지 않는 채팅방ID"));
        //보내는 사람을 찾음
        Member me = memberRepository.findById(sendRequestDto.getSenderId()).orElseThrow(
                () -> new CustomException(String.valueOf(roomId), "존재하지 않는 멤버ID"));
        // 받는 사람을 찾음
        Member other = chatRoom.getMemberA() == me ? chatRoom.getMemberB() : chatRoom.getMemberA();

        // 채팅방 입장 Logic
        if(type == MessageType.ENTER) {
            // WebSocket Session에 userId, roomId 저장
            headerAccessor.getSessionAttributes().put("userId", sendRequestDto.getSenderId());
            headerAccessor.getSessionAttributes().put("roomId", roomId);
        }
        // 채팅 메시지 전송 Logic
        else {
            Chat chat = Chat.builder()
                    .id(UUID.randomUUID().toString())
                    .roomId(roomId)
                    .senderId(sendRequestDto.getSenderId())
                    .message(sendRequestDto.getMessage())
                    .url(sendRequestDto.getImg())
                    .build();

            template.convertAndSend("/sub/" + roomId, SendResponseDto.of(chatRepository.save(chat)));

            //받는 사람에게 sse로 알림 전송, TALK일 때만 알림 전송
            //other가 채팅방에 접속한 경우 (두 명 다 채팅방에 접속한 경우)만 SSE 알림 전송
            if (chatRoom.isAIsPartIn() && chatRoom.isBIsPartIn()) {
                sseService.sendEventToClient(NotificationType.NEW_CHAT, other.getMemberId(), me.getNickname() + "님이 메시지를 보냈습니다.");
            }
        }
    }

    // WebSocket 연결 해제
    public void webSocketDisconnectListener(SessionDisconnectEvent event) {
        // WebSocket Session에서 userId, roomId 가져오기
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Long userId = Long.parseLong(headerAccessor.getSessionAttributes().get("userId").toString());
        Long roomId = Long.parseLong(headerAccessor.getSessionAttributes().get("roomId").toString());

        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(()->new CustomException(String.valueOf(roomId),"존재하지 않는 채팅방ID"));

        String identity = "A";
        if(chatRoom.getMemberB().getMemberId().equals(userId)) { identity = "B"; }

        // 연결 해제 전, 마지막 읽은 메시지 ID를 chatRoom 테이블에 저장
        String lastChatId = chatRepository.findTopByRoomId(roomId).getId();

        if (identity.equals("A")) {
            chatRoom.changeALastReadId(lastChatId);
        } else {
            chatRoom.changeBLastReadId(lastChatId);
        }
    }
}
