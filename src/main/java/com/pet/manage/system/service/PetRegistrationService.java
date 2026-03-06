package com.pet.manage.system.service;

import com.pet.manage.system.dtos.OwnerResponseDto;
import com.pet.manage.system.dtos.PetRegistrationDto;

public interface PetRegistrationService {

    OwnerResponseDto registerPet(PetRegistrationDto petRegistrationDto);

}
