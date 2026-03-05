package com.pet.manage.system.service.Impl;

import com.pet.manage.system.dtos.VeterinaryRegistrationRequestDto;
import com.pet.manage.system.dtos.VeterinaryRegistrationResponseDto;
import com.pet.manage.system.entity.Owner;
import com.pet.manage.system.repository.OwnerRepository;
import com.pet.manage.system.service.VeterinaryRegistrationService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VeterinaryRegistrationServiceImpl implements VeterinaryRegistrationService {

    @Autowired
    private OwnerRepository ownerRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public VeterinaryRegistrationResponseDto saveRegistration(VeterinaryRegistrationRequestDto dto) {
        // Map DTO to Entity
        Owner entity = modelMapper.map(dto, Owner.class);
        // Save Entity
        Owner savedEntity = ownerRepository.save(entity);
        // Map Entity to Response DTO
        return modelMapper.map(savedEntity, VeterinaryRegistrationResponseDto.class);
    }
}