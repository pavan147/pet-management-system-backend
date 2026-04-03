package com.pet.manage.system.service;

import com.pet.manage.system.dtos.*;
import com.pet.manage.system.dtos.request.AppointmentRequestDTO;
import com.pet.manage.system.dtos.response.AppointmentResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface PetService {

    OwnerResponseDto registerPet(PetRegistrationDto petRegistrationDto , MultipartFile petPhoto) throws IOException;

    PetVaccinationRecorResponsedDTO saveVaccinationRecord(PetVaccinationRecorRequestdDTO petVaccinationRecorRequestdDTO);

     void savePetMedicalRecord(PetMedicalRequestDto petMedicalRequestDto);

    PetMedicalRespnseDto  savePetMedicalDetails(PetMedicalRequestDto petMedicalRequestDto);

    AppointmentResponseDTO bookAppointment(AppointmentRequestDTO appointmentRequestDTO);

    List<AppointmentResponseDTO> getAppointmentsByDate(String date);

    AppointmentResponseDTO updateStatus(Long id, String status, String action);
}
