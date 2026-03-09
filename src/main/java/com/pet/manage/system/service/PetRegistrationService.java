package com.pet.manage.system.service;

import com.pet.manage.system.dtos.OwnerResponseDto;
import com.pet.manage.system.dtos.PetRegistrationDto;
import com.pet.manage.system.dtos.PetVaccinationRecorRequestdDTO;
import com.pet.manage.system.dtos.PetVaccinationRecorResponsedDTO;
import com.pet.manage.system.entity.PetVaccinationRecord;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface PetRegistrationService {

    OwnerResponseDto registerPet(PetRegistrationDto petRegistrationDto , MultipartFile petPhoto) throws IOException;

    PetVaccinationRecorResponsedDTO saveVaccinationRecord(PetVaccinationRecorRequestdDTO petVaccinationRecorRequestdDTO);

}
