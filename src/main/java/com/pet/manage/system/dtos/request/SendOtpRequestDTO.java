package com.pet.manage.system.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SendOtpRequestDTO {

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;
}

