package com.pet.manage.system.dtos;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class MedicalChatPetContextDto {

    private Long id;
    private String petName;
    private String petType;
    private String breed;
    private String gender;
    private LocalDate dob;
    private LocalDate registrationDate;
    private String description;
    private String allergies;
    private String photoBase64;
    private String photoContentType;
    private String ownerName;
    private String ownerEmail;
    private String ownerPhoneNumber;
    private String assignedVetName;
}

