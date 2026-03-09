package com.pet.manage.system.service.Impl;

import com.pet.manage.system.Utils;
import com.pet.manage.system.dtos.OwnerResponseDto;
import com.pet.manage.system.dtos.PetRegistrationDto;
import com.pet.manage.system.dtos.PetVaccinationRecorRequestdDTO;
import com.pet.manage.system.dtos.PetVaccinationRecorResponsedDTO;
import com.pet.manage.system.entity.Owner;
import com.pet.manage.system.entity.Pet;
import com.pet.manage.system.entity.PetVaccinationRecord;
import com.pet.manage.system.repository.OwnerRepository;
import com.pet.manage.system.repository.PetRepository;
import com.pet.manage.system.repository.PetVaccinationRecordRepository;
import com.pet.manage.system.service.HelperUtilService;
import com.pet.manage.system.service.PetRegistrationService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class PetRegistrationServiceImpl implements PetRegistrationService {

    @Autowired
    private OwnerRepository ownerRepository;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private PetVaccinationRecordRepository vaccinationRecordRepository;

    @Autowired
    private HelperUtilService helperUtilService;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public OwnerResponseDto registerPet(PetRegistrationDto petRegistrationDto , MultipartFile petPhoto) throws IOException {

        Owner owner =  helperUtilService.findOwnerByContact(petRegistrationDto.getOwnerContact());

        if (owner == null) {
            throw new RuntimeException("Owner not found with provided contact.");
        }
        Pet petEntity = modelMapper.map(petRegistrationDto, Pet.class);
        petEntity.setOwner(owner);
        // Save photo if present
        if (petPhoto != null && !petPhoto.isEmpty()) {
            petEntity.setPhoto(petPhoto.getBytes()); // You may want to handle IOException
            petEntity.setPhotoContentType(petPhoto.getContentType());
        }
        petRepository.save(petEntity);

        return Utils.getOwnerDetails(owner, modelMapper);
    }


    @Override
    public PetVaccinationRecorResponsedDTO saveVaccinationRecord(PetVaccinationRecorRequestdDTO petVaccinationRecorRequestdDTO) {

        Owner owner =  helperUtilService.findOwnerByContact(petVaccinationRecorRequestdDTO.getOwnerContact());
        if (owner == null) {
            throw new RuntimeException("Owner not found with provided contact.");
        }
        PetVaccinationRecord petVaccinationRecordEntity = modelMapper.map(petVaccinationRecorRequestdDTO, PetVaccinationRecord.class);
        owner.getPets().stream()
                .filter(pet -> pet.getId().equals(petVaccinationRecorRequestdDTO.getPetId()))
                .findFirst()
                .ifPresentOrElse(
                        pet -> petVaccinationRecordEntity.setPet(pet),
                        () -> { throw new RuntimeException("Pet not found with provided name for the owner."); }
                );

        PetVaccinationRecord petVaccinationRecordSave = vaccinationRecordRepository.save(petVaccinationRecordEntity);

        return  modelMapper.map(petVaccinationRecordSave, PetVaccinationRecorResponsedDTO.class);
    }
}