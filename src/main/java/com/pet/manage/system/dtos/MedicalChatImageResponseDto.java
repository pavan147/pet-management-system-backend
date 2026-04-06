package com.pet.manage.system.dtos;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MedicalChatImageResponseDto {

    private Long id;
    private Long petId;
    private String fileName;
    private String contentType;
    private Long sizeBytes;
    private String senderName;
    private String senderRole;
    private LocalDateTime uploadedAt;
    private String imageUrl;
}

