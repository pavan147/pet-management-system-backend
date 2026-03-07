package com.pet.manage.system.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PetRegistrationDto {
    // Owner fields
    private String ownerContact; // email or phone
    // Pet fields
    private String petName;
    private String petType;
    private String otherPetType; // for "Other" pet type
    private String breed;
    private String sex;
    private String color;
    private String description;
    //@JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dob;
    private Double weight;

}