package com.pet.manage.system.dtos;

import lombok.Data;
import java.time.LocalDate;

@Data
public class PetVaccinationRecorRequestdDTO {

    private Long petId;
    private String ownerContact;
    private String vaccination;
    private String vaccineName;
    private String brandAndDoses;
    private LocalDate vaccinationDate;
    private Integer durationMonths;
    private LocalDate validTill;
    private Integer weight;
}