package com.pet.manage.system.dtos;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class DoctorPetSearchResponseDto {
    private Long petId;
    private String petName;
    private String petType;
    private String breed;
    private String ownerName;
    private String ownerPhoneNumber;
    private String ownerEmail;
    private String assignedVetName;
    private LocalDate lastVisitDate;
}

