package com.pet.manage.system.service;

import com.pet.manage.system.dtos.VeterinaryRegistrationRequestDto;
import com.pet.manage.system.dtos.VeterinaryRegistrationResponseDto;
import com.pet.manage.system.entity.Owner;

public interface OwnerService  {

    VeterinaryRegistrationResponseDto saveRegistration(VeterinaryRegistrationRequestDto dto);

    Owner getOwnerByEmailOrPhone(String email, String phoneNumber);
}
