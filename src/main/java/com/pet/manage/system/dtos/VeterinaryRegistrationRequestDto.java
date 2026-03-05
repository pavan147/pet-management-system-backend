package com.pet.manage.system.dtos;

import lombok.Data;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

@Data
public class VeterinaryRegistrationRequestDto {
   // @NotBlank
    private String ownerName;

    //@Email
   // @NotBlank
    private String email;

   // @NotBlank
    private String phoneNumber;

   // @NotBlank
    private String password;

    //@NotBlank
    private String petName;

   // @NotBlank
    private String petType;

    private String otherPetType;

   // @NotBlank
    private String breed;

   // @NotBlank
    private String address;

    //@NotNull
    private LocalDate registrationDate;
}