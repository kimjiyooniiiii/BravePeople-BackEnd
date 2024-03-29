package com.example.brave_people_backend.entity;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.time.LocalDateTime;

@Getter
@Builder
@Document(collection = "chat")
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Chat {
    @Id
    @Field(value = "_id", targetType = FieldType.STRING)
    private String id;

    @Field("room_id")
    private Long roomId;

    @Field("sender_id")
    private Long senderId;

    @Field("message")
    private String message;

    @Field("send_at")
    @CreatedDate
    private LocalDateTime sendAt;

    @Field("url")
    private String url;

    public void setMessageWhenImage(String message) {
        this.message = message;
    }
}
