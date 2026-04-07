package com.pet.manage.system.dtos.response;

import com.pet.manage.system.dtos.OwnerResponseDto;
import com.pet.manage.system.dtos.LabTestReportResponseDto;
import com.pet.manage.system.dtos.PetMedicalRespnseDto;
import com.pet.manage.system.dtos.PetResponseDto;
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
     private List<PetResponseDto> pets;
     // Owner details
     private OwnerResponseDto owner;
      //appoinmet booked date all
     private List<AppointmentResponseDTO> appointments;
    private List<PetVaccinationRecorResponsedDTO> vaccinations;
    // pet medical and its prescription details
    private List<PetMedicalRespnseDto> medicalRecords;
    private List<LabTestReportResponseDto> labTestReports;
}