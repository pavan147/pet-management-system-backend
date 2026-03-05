package com.pet.manage.system.dtos;

import lombok.Data;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

@Data
public class VeterinaryRegistrationResponseDto {

    private String ownerName;


    private String email;


    private String phoneNumber;


    private String password;


//    private String petName;
//
//
//    private String petType;
//
//    private String otherPetType;
//
//
//    private String breed;
//
//
//    private String address;
//
//
//    private LocalDate registrationDate;
}