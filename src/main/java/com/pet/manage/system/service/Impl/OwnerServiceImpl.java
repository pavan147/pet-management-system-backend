package com.pet.manage.system.service.Impl;

import com.pet.manage.system.Utils;
import com.pet.manage.system.commons.Constants;
import com.pet.manage.system.dtos.OwnerResponseDto;
import com.pet.manage.system.dtos.VeterinaryRegistrationRequestDto;
import com.pet.manage.system.dtos.VeterinaryRegistrationResponseDto;
import com.pet.manage.system.entity.Owner;
import com.pet.manage.system.entity.Role;
import com.pet.manage.system.global.exception.DuplicateOwnerException;
import com.pet.manage.system.repository.OwnerRepository;
import com.pet.manage.system.repository.RoleRepository;
import com.pet.manage.system.service.OwnerService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Service
public class OwnerServiceImpl implements OwnerService {

    @Autowired
    private OwnerRepository ownerRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;


    @Override
    public VeterinaryRegistrationResponseDto saveRegistration(VeterinaryRegistrationRequestDto dto) {
        Map<String, String> duplicateErrors = new LinkedHashMap<>();
        if (ownerRepository.existsByEmail(dto.getEmail())) {
            duplicateErrors.put("email", "This email is already registered. Please use a different email.");
        }
        if (ownerRepository.existsByPhoneNumber(dto.getPhoneNumber())) {
            duplicateErrors.put("phoneNumber", "This phone number is already registered. Please use a different number.");
        }

        if (!duplicateErrors.isEmpty()) {
            throw new DuplicateOwnerException(duplicateErrors);
        }

        String encodedPassword = passwordEncoder.encode(dto.getPassword());
        // Map DTO to Entity
        Owner entity = modelMapper.map(dto, Owner.class);
        entity.setPassword(encodedPassword);
        Role role = roleRepository.findByName(Constants.PET_OWNER_ROLE);
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        entity.setRoles(roles);
        // Save Entity
        Owner savedEntity = ownerRepository.save(entity);

        // Map Entity to Response DTO
        return modelMapper.map(savedEntity, VeterinaryRegistrationResponseDto.class);
    }

    @Override
    public OwnerResponseDto getOwnerByEmailOrPhone(String email, String phoneNumber) {
        Owner owner = null;
        if (email != null && !email.isEmpty()) {
            owner = ownerRepository.findByEmail(email).get();
        } else if (phoneNumber != null && !phoneNumber.isEmpty()) {
            owner = ownerRepository.findByPhoneNumber(phoneNumber).get();
        }
        return Utils.getOwnerDetails(owner , modelMapper);
    }


}