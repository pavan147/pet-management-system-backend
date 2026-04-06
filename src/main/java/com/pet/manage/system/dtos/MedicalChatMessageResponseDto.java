package com.pet.manage.system.dtos;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class MedicalChatMessageResponseDto {

    private Long id;
    private Long petId;
    private String senderName;
    private String senderRole;
    private String message;
    private boolean emergency;
    private Long linkedImageId;
    private LocalDateTime createdAt;
    private List<MedicalChatImageResponseDto> images;
}

