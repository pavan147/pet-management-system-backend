package com.pet.manage.system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pet.manage.system.dtos.OwnerResponseDto;
import com.pet.manage.system.dtos.PetRegistrationDto;
import com.pet.manage.system.entity.Pet;
import com.pet.manage.system.service.PetRegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/pets")
@CrossOrigin(origins = "http://localhost:5173")
public class PetController {

    @Autowired
    private PetRegistrationService petRegistrationService;

    @PostMapping(value = "/register", consumes = {"multipart/form-data"})
    public ResponseEntity<OwnerResponseDto> registerPet(
            @RequestPart("petRegistrationDto") String petRegistrationDtoStr,
            @RequestPart(value = "petPhoto", required = false) MultipartFile petPhoto) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        PetRegistrationDto petRegistrationDto = mapper.readValue(petRegistrationDtoStr, PetRegistrationDto.class);
        OwnerResponseDto ownerResponseDto = petRegistrationService.registerPet(petRegistrationDto, petPhoto);
        return ResponseEntity.ok(ownerResponseDto);
    }
}