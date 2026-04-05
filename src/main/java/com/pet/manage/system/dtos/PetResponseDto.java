package com.pet.manage.system.dtos;

import lombok.Data;

import java.time.LocalDate;

@Data
public class PetResponseDto {

    private Long id;
    private String petName;
    private String petType;
    private String otherPetType;
    private String breed;
    private LocalDate registrationDate;
    private String photoBase64;
    private String photoContentType;
}
