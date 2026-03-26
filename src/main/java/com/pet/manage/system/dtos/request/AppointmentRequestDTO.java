// src/main/java/com/example/dto/AppointmentDTO.java
package com.pet.manage.system.dtos.request;

import lombok.Data;

@Data
public class AppointmentRequestDTO {
    private String name;
    private String email;
    private String phone;
    private String date; // Format: dd/MM/yyyy
    private String time;
    private String reason;
}