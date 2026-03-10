package com.pet.manage.system.service;

import com.pet.manage.system.dtos.*;
import com.pet.manage.system.entity.PetVaccinationRecord;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface PetRegistrationService {

    OwnerResponseDto registerPet(PetRegistrationDto petRegistrationDto , MultipartFile petPhoto) throws IOException;

    PetVaccinationRecorResponsedDTO saveVaccinationRecord(PetVaccinationRecorRequestdDTO petVaccinationRecorRequestdDTO);

     void savePetMedicalRecord(PetMedicalRequestDto petMedicalRequestDto);
}
