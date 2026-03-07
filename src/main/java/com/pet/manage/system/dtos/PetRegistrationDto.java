package com.pet.manage.system.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PetRegistrationDto {
    @NotBlank(message = "Owner contact is required")
    private String ownerContact;

    @NotBlank(message = "Pet name is required")
    private String petName;

    @NotBlank(message = "Pet type is required")
    private String petType;

    // Only required if petType is "Other" - handle this in service or with a custom validator
    private String otherPetType;

    @NotBlank(message = "Breed is required")
    private String breed;

    @NotBlank(message = "Sex is required")
    private String sex;

    @NotBlank(message = "Color is required")
    private String color;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Date of birth is required")
    private LocalDate dob;

    @NotNull(message = "Weight is required")
    @DecimalMin(value = "0.1", message = "Weight must be greater than 0")
    private Double weight;
}