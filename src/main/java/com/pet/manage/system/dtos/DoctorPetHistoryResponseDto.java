package com.pet.manage.system.dtos;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DoctorPetHistoryResponseDto {
    private Long petId;
    private String petName;
    private String petType;
    private String breed;
    private String allergies;
    private String ownerName;
    private String ownerPhoneNumber;
    private String ownerEmail;
    private String assignedVetName;
    private List<PetMedicalRespnseDto> medicalHistory;
}

