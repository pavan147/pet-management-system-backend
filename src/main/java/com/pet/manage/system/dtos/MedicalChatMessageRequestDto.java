package com.pet.manage.system.dtos;

import lombok.Data;

@Data
public class MedicalChatMessageRequestDto {

    private String message;
    private boolean emergency;
    private Long linkedImageId;
}

