// src/main/java/com/example/dto/AppointmentDTO.java
package com.pet.manage.system.dtos.response;

import lombok.Data;

@Data
public class AppointmentResponseDTO {
    private Long id; // <-- Add this line!
    private String name;
    private String email;
    private String phone;
    private String date; // Format: dd/MM/yyyy
    private String time;
    private String reason;

    private String status; // waiting, checked-in, pending
    private String action;
}