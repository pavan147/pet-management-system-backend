package com.pet.manage.system.service;

import com.pet.manage.system.dtos.VeterinaryRegistrationRequestDto;
import com.pet.manage.system.dtos.VeterinaryRegistrationResponseDto;

public interface VeterinaryRegistrationService {

    VeterinaryRegistrationResponseDto saveRegistration(VeterinaryRegistrationRequestDto dto);
}
