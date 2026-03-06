package com.pet.manage.system.controller;

import com.pet.manage.system.dtos.OwnerResponseDto;
import com.pet.manage.system.dtos.PetRegistrationDto;
import com.pet.manage.system.entity.Pet;
import com.pet.manage.system.service.PetRegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pets")
@CrossOrigin(origins = "http://localhost:5173")
public class PetRegistrationController {

    @Autowired
    private PetRegistrationService petRegistrationService;

    @PostMapping("/register")
    public ResponseEntity<OwnerResponseDto> registerPet(@RequestBody PetRegistrationDto petRegistrationDto) {
        OwnerResponseDto ownerResponseDto = petRegistrationService.registerPet(petRegistrationDto);
        return ResponseEntity.ok(ownerResponseDto);

    }
}