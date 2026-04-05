package com.pet.manage.system.dtos;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class PetMedicalRespnseDto {
    private Long petMedicalId;
    private String  ownerContact;
    private Long petId;
    private String allergies;
    private String diagnosis;
    private List<PrescriptionDTO> prescriptions;
    private String treatmentSuggestions;
    private LocalDate validateTill;
    private LocalDate visitDate;

}


