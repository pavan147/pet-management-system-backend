package com.pet.manage.system.dtos;

import lombok.Data;
import java.time.LocalDate;

@Data
public class PetVaccinationRecorResponsedDTO {
    
    private Long petId;
    private String ownerContact;
    private String vaccination;
    private String brandAndDoses;
    private LocalDate vaccinationDate;
    private Integer durationMonths;
    private LocalDate validTill;
    private Integer weight;
}