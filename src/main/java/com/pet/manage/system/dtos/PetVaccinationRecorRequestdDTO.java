package com.pet.manage.system.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class PetVaccinationRecorRequestdDTO {

    @NotNull(message = "Please select your Pet")
    private Long petId;

    @NotBlank(message = "Owner contact must be provided and cannot be blank.")
    private String ownerContact;

    @NotBlank(message = "Vaccination type must be provided and cannot be blank.")
    private String vaccination;

    @NotBlank(message = "Vaccine name must be provided and cannot be blank.")
    private String vaccineName;

    @NotBlank(message = "Brand and doses information must be provided and cannot be blank.")
    private String brandAndDoses;

    @NotNull(message = "Vaccination date must be provided.")
    private LocalDate vaccinationDate;

    @NotNull(message = "Duration in months must be provided.")
    @Min(value = 1, message = "Duration in months must be at least 1.")
    private Integer durationMonths;

    @NotNull(message = "Valid till date must be provided.")
    private LocalDate validTill;

    @NotNull(message = "Pet weight must be provided.")
    private Integer weight;
}