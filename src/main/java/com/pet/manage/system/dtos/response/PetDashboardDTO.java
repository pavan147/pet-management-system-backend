package com.pet.manage.system.dtos.response;

import com.pet.manage.system.dtos.PetVaccinationRecorResponsedDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PetDashboardDTO {
     //Pet details
     // Owner details
      //appoinmet booked date all
    private List<PetVaccinationRecorResponsedDTO> vaccinations;
    // pet medical and its prescription details
    //
}