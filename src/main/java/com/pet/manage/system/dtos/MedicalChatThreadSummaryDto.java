package com.pet.manage.system.dtos;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MedicalChatThreadSummaryDto {

    private Long threadId;
    private Long petId;
    private String petName;
    private String title;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime closedAt;
    private String closedByName;
    private String latestMessage;
    private LocalDateTime latestMessageAt;
    private boolean hasEmergency;
    private long messageCount;
}

