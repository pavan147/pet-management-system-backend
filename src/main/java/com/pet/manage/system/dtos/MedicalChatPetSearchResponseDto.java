package com.pet.manage.system.dtos;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MedicalChatPetSearchResponseDto {

    private Long petId;
    private String petName;
    private String petType;
    private String breed;
    private String ownerName;
    private String ownerPhoneNumber;
    private String assignedVetName;
    private LocalDateTime latestMessageAt;
    private String latestMessage;
    private Long latestThreadId;
    private String latestThreadTitle;
    private boolean emergency;
    private String chatStatus;
    private LocalDateTime closedAt;
    private String closedByName;
}
