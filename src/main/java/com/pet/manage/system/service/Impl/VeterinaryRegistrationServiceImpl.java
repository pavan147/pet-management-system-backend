package com.pet.manage.system.service.Impl;

import com.pet.manage.system.dtos.VeterinaryRegistrationRequestDto;
import com.pet.manage.system.dtos.VeterinaryRegistrationResponseDto;
import com.pet.manage.system.entity.VeterinaryRegistration;
import com.pet.manage.system.repository.VeterinaryRegistrationRepository;
import com.pet.manage.system.service.VeterinaryRegistrationService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VeterinaryRegistrationServiceImpl implements VeterinaryRegistrationService {

    @Autowired
    private VeterinaryRegistrationRepository veterinaryRegistrationRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public VeterinaryRegistrationResponseDto saveRegistration(VeterinaryRegistrationRequestDto dto) {
        // Map DTO to Entity
        VeterinaryRegistration entity = modelMapper.map(dto, VeterinaryRegistration.class);
        // Save Entity
        VeterinaryRegistration savedEntity = veterinaryRegistrationRepository.save(entity);
        // Map Entity to Response DTO
        return modelMapper.map(savedEntity, VeterinaryRegistrationResponseDto.class);
    }
}