package com.pet.manage.system.dtos;

import lombok.Data;

@Data
public class StatusUpdateRequest {
    private String status;
    private String action;
}