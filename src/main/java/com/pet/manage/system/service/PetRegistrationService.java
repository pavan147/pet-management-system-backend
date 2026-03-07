package com.pet.manage.system.service;

import com.pet.manage.system.dtos.OwnerResponseDto;
import com.pet.manage.system.dtos.PetRegistrationDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface PetRegistrationService {

    OwnerResponseDto registerPet(PetRegistrationDto petRegistrationDto , MultipartFile petPhoto) throws IOException;

}
