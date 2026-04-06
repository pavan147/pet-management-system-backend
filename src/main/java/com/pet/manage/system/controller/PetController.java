package com.pet.manage.system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pet.manage.system.Utils;
import com.pet.manage.system.dtos.*;
import com.pet.manage.system.dtos.request.AppointmentRequestDTO;
import com.pet.manage.system.dtos.response.AppointmentResponseDTO;
import com.pet.manage.system.dtos.response.PetDashboardDTO;
import com.pet.manage.system.service.PetService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.util.List;
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
    public ResponseEntity<PetMedicalRespnseDto> savePetMedicalDetails(@RequestBody PetMedicalRequestDto petMedicalRequestDto) {
        PetMedicalRespnseDto response = petService.savePetMedicalDetails(petMedicalRequestDto);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/book-appointment")
    public AppointmentResponseDTO createAppointment(@Valid  @RequestBody AppointmentRequestDTO dto) {
        return petService.bookAppointment(dto);
    }

    @GetMapping("/appointments")
    public List<AppointmentResponseDTO> getAppointments(@RequestParam(required = false) String date) {
        return petService.getAppointmentsByDate(date);
    }

    // Update status and action
    @PutMapping("/appointments/{id}/status")
    public AppointmentResponseDTO updateStatus(@PathVariable Long id, @RequestBody StatusUpdateRequest request) {
        return petService.updateStatus(id, request.getStatus(), request.getAction());
    }

    // Check if owner is registered
    @GetMapping("/owner/check")
    public boolean isOwnerRegistered(@RequestParam String email) {
        return petService.isOwnerRegistered(email);
    }

    @GetMapping("/dashboard")
    public ResponseEntity<PetDashboardDTO> getDashboard() {
        PetDashboardDTO dashboard = petService.getDashboardData();
        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/medical-details/prescription-pdf")
    public ResponseEntity<byte[]> downloadPrescriptionPdf(
            @RequestParam Long petId,
            @RequestParam Long petMedicalId
    ) {
        byte[] pdfBytes = petService.downloadPrescriptionPdf(petId, petMedicalId);
        String fileName = "pet_" + petId + "_medical_" + petMedicalId + "_prescription.pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(fileName).build().toString())
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @GetMapping("/medical-chat/{petId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_PET_OWNER','ROLE_DOCTOR')")
    public ResponseEntity<MedicalChatThreadResponseDto> getMedicalChatThread(@PathVariable Long petId) {
        return ResponseEntity.ok(petService.getMedicalChatThread(petId));
    }

    @GetMapping("/medical-chat/search")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_DOCTOR')")
    public ResponseEntity<List<MedicalChatPetSearchResponseDto>> searchMedicalChatPets(
            @RequestParam(required = false) String query,
            @RequestParam(required = false, defaultValue = "ACTIVE") String status
    ) {
        return ResponseEntity.ok(petService.searchMedicalChatPets(query, status));
    }

    @PutMapping("/medical-chat/{petId}/close")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_PET_OWNER')")
    public ResponseEntity<MedicalChatThreadResponseDto> closeMedicalChat(@PathVariable Long petId) {
        return ResponseEntity.ok(petService.closeMedicalChat(petId));
    }

    @PostMapping("/medical-chat/{petId}/messages")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_PET_OWNER','ROLE_DOCTOR')")
    public ResponseEntity<MedicalChatMessageResponseDto> sendMedicalChatMessage(
            @PathVariable Long petId,
            @RequestBody MedicalChatMessageRequestDto requestDto
    ) {
        return ResponseEntity.ok(petService.sendMedicalChatMessage(petId, requestDto));
    }

    @PostMapping(value = "/medical-chat/{petId}/images", consumes = {"multipart/form-data"})
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_PET_OWNER')")
    public ResponseEntity<MedicalChatMessageResponseDto> uploadMedicalChatImages(
            @PathVariable Long petId,
            @RequestPart("files") MultipartFile[] files,
            @RequestPart(value = "message", required = false) String message,
            @RequestPart(value = "emergency", required = false) String emergency
    ) {
        boolean emergencyFlag = Boolean.parseBoolean(emergency);
        return ResponseEntity.ok(petService.uploadMedicalChatImages(petId, files, message, emergencyFlag));
    }

    @GetMapping("/medical-chat/images/{imageId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_PET_OWNER','ROLE_DOCTOR')")
    public ResponseEntity<byte[]> getMedicalChatImage(@PathVariable Long imageId) {
        byte[] data = petService.getMedicalChatImage(imageId);
        String contentType = petService.getMedicalChatImageContentType(imageId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename("pet_medical_chat_" + imageId).build().toString())
                .contentType(MediaType.parseMediaType(contentType))
                .body(data);
    }

    @PutMapping("/{petId}/assign-vet/{vetId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> assignVetToPet(@PathVariable Long petId, @PathVariable Long vetId) {
        petService.assignVetToPet(petId, vetId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/medical-chat/emergency-feed")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_DOCTOR')")
    public ResponseEntity<List<MedicalChatMessageResponseDto>> getEmergencyFeed() {
        return ResponseEntity.ok(petService.getEmergencyMedicalChatFeed());
    }

    // ---- Thread-based endpoints ----

    @GetMapping("/{petId}/medical-chat/threads")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_PET_OWNER','ROLE_DOCTOR')")
    public ResponseEntity<List<MedicalChatThreadSummaryDto>> getThreadsForPet(@PathVariable Long petId) {
        return ResponseEntity.ok(petService.getThreadsForPet(petId));
    }

    @PostMapping("/{petId}/medical-chat/threads")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_PET_OWNER')")
    public ResponseEntity<MedicalChatThreadResponseDto> createThread(
            @PathVariable Long petId,
            @RequestBody(required = false) MedicalChatThreadCreateRequestDto requestDto
    ) {
        String title = requestDto != null ? requestDto.getTitle() : null;
        return ResponseEntity.ok(petService.createThread(petId, title));
    }

    @GetMapping("/medical-chat/threads/{threadId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_PET_OWNER','ROLE_DOCTOR')")
    public ResponseEntity<MedicalChatThreadResponseDto> getThread(@PathVariable Long threadId) {
        return ResponseEntity.ok(petService.getThreadById(threadId));
    }

    @PostMapping("/medical-chat/threads/{threadId}/messages")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_PET_OWNER','ROLE_DOCTOR')")
    public ResponseEntity<MedicalChatMessageResponseDto> sendMessageToThread(
            @PathVariable Long threadId,
            @RequestBody MedicalChatMessageRequestDto requestDto
    ) {
        return ResponseEntity.ok(petService.sendMessageToThread(threadId, requestDto));
    }

    @PostMapping(value = "/medical-chat/threads/{threadId}/images", consumes = {"multipart/form-data"})
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_PET_OWNER')")
    public ResponseEntity<MedicalChatMessageResponseDto> uploadImagesToThread(
            @PathVariable Long threadId,
            @RequestPart("files") MultipartFile[] files,
            @RequestPart(value = "message", required = false) String message,
            @RequestPart(value = "emergency", required = false) String emergency
    ) {
        boolean emergencyFlag = Boolean.parseBoolean(emergency);
        return ResponseEntity.ok(petService.uploadImagesToThread(threadId, files, message, emergencyFlag));
    }

    @PutMapping("/medical-chat/threads/{threadId}/close")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_PET_OWNER')")
    public ResponseEntity<MedicalChatThreadResponseDto> closeThread(@PathVariable Long threadId) {
        return ResponseEntity.ok(petService.closeThread(threadId));
    }
}