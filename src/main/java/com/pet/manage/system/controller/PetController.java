package com.pet.manage.system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pet.manage.system.Utils;
import com.pet.manage.system.dtos.*;
import com.pet.manage.system.dtos.request.AppointmentRequestDTO;
import com.pet.manage.system.dtos.response.AppointmentResponseDTO;
import com.pet.manage.system.service.PetService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.util.Set;

@RestController
@RequestMapping("/api/pets")
@CrossOrigin(origins = "http://localhost:5173")
public class PetController {

    @Autowired
    private PetService petService;

    @Autowired
    private Validator validator; // Add this


    @PostMapping(value = "/register", consumes = {"multipart/form-data"})
    public ResponseEntity<?> registerPet(
            @RequestPart("petRegistrationDto") String petRegistrationDtoStr,
            @RequestPart(value = "petPhoto", required = false) MultipartFile petPhoto) throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        PetRegistrationDto petRegistrationDto = mapper.readValue(petRegistrationDtoStr, PetRegistrationDto.class);

        // Validate DTO
        Set<ConstraintViolation<PetRegistrationDto>> violations = validator.validate(petRegistrationDto);
        if (!violations.isEmpty()) {
            // Return validation errors
            StringBuilder sb = new StringBuilder();
            for (ConstraintViolation<PetRegistrationDto> violation : violations) {
                sb.append(violation.getPropertyPath()).append(": ").append(violation.getMessage()).append("; ");
            }

            return ResponseEntity.badRequest().body(Utils.errorStringToJson(sb.toString()));
        }

        OwnerResponseDto ownerResponseDto = petService.registerPet(petRegistrationDto, petPhoto);
        return ResponseEntity.ok(ownerResponseDto);
    }



    @PostMapping("/vaccination-record")
    public ResponseEntity<PetVaccinationRecorResponsedDTO> saveVaccinationRecord(@Valid @RequestBody PetVaccinationRecorRequestdDTO petVaccinationRecorRequestdDTO) {
        PetVaccinationRecorResponsedDTO petVaccinationRecorResponsedDTO = petService.saveVaccinationRecord(petVaccinationRecorRequestdDTO);
        return ResponseEntity.ok(petVaccinationRecorResponsedDTO);
    }


    @PostMapping("/medical-details")
    public ResponseEntity<PetVaccinationRecorResponsedDTO> savePetMedicalDetails(@RequestBody PetMedicalRequestDto petMedicalRequestDto) {
        petService.savePetMedicalDetails(petMedicalRequestDto);
        return ResponseEntity.ok(null);
    }


    @PostMapping("/book-appointment")
    public AppointmentResponseDTO createAppointment(@RequestBody AppointmentRequestDTO dto) {
        return petService.bookAppointment(dto);
    }
}