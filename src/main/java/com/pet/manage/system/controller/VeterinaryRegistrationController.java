package com.pet.manage.system.controller;


import com.pet.manage.system.dtos.VeterinaryRegistrationRequestDto;
import com.pet.manage.system.dtos.VeterinaryRegistrationResponseDto;
import com.pet.manage.system.service.VeterinaryRegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/registrations")
public class VeterinaryRegistrationController {

    @Autowired
    private VeterinaryRegistrationService veterinaryRegistrationService;

    /**
     * Endpoint to save veterinary registration data.
     */
    @PostMapping
    public ResponseEntity<VeterinaryRegistrationResponseDto> registerPet(@Validated @RequestBody VeterinaryRegistrationRequestDto dto) {
      VeterinaryRegistrationResponseDto veterinaryRegistrationResponseDto = veterinaryRegistrationService.saveRegistration(dto);
        return ResponseEntity.ok(veterinaryRegistrationResponseDto);
    }
}