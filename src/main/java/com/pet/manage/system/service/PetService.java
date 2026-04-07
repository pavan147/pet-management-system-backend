package com.pet.manage.system.service;

import com.pet.manage.system.dtos.*;
import com.pet.manage.system.dtos.request.AppointmentRequestDTO;
import com.pet.manage.system.dtos.response.AppointmentResponseDTO;
import com.pet.manage.system.dtos.response.PetDashboardDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface PetService {

    OwnerResponseDto registerPet(PetRegistrationDto petRegistrationDto , MultipartFile petPhoto) throws IOException;

    PetVaccinationRecorResponsedDTO saveVaccinationRecord(PetVaccinationRecorRequestdDTO petVaccinationRecorRequestdDTO);

     void savePetMedicalRecord(PetMedicalRequestDto petMedicalRequestDto);

    PetMedicalRespnseDto  savePetMedicalDetails(PetMedicalRequestDto petMedicalRequestDto);

    AppointmentResponseDTO bookAppointment(AppointmentRequestDTO appointmentRequestDTO);

    List<AppointmentResponseDTO> getAppointmentsByDate(String date);

    AppointmentResponseDTO updateStatus(Long id, String status, String action);

    boolean isOwnerRegistered(String email);

    PetDashboardDTO getDashboardData();

    byte[] downloadPrescriptionPdf(Long petId, Long petMedicalId);

    LabTestReportResponseDto uploadLabTestReport(Long petId,
                                                 MultipartFile file,
                                                 String title,
                                                 String labTestType,
                                                 String ownerNotes);

    List<LabTestReportResponseDto> getLabTestReportsForPet(Long petId);

    List<LabTestReportResponseDto> searchLabTestReportsForDoctor(String query, String status);

    LabTestReportResponseDto getLabTestReport(Long labTestReportId);

    LabTestReportResponseDto reviewLabTestReport(Long labTestReportId, LabTestReviewRequestDto requestDto);

    byte[] downloadLabTestReport(Long labTestReportId);

    String getLabTestReportContentType(Long labTestReportId);

    String getLabTestReportFileName(Long labTestReportId);

    MedicalChatThreadResponseDto getMedicalChatThread(Long petId);

    List<MedicalChatPetSearchResponseDto> searchMedicalChatPets(String query, String status);

    MedicalChatThreadResponseDto closeMedicalChat(Long petId);

    MedicalChatMessageResponseDto sendMedicalChatMessage(Long petId, MedicalChatMessageRequestDto requestDto);

    MedicalChatMessageResponseDto uploadMedicalChatImages(Long petId,
                                                          MultipartFile[] files,
                                                          String message,
                                                          boolean emergency);

    byte[] getMedicalChatImage(Long imageId);

    String getMedicalChatImageContentType(Long imageId);

    void assignVetToPet(Long petId, Long vetId);

    List<MedicalChatMessageResponseDto> getEmergencyMedicalChatFeed();

    // ---- Thread-based operations ----

    List<MedicalChatThreadSummaryDto> getThreadsForPet(Long petId);

    MedicalChatThreadResponseDto createThread(Long petId, String title);

    MedicalChatThreadResponseDto getThreadById(Long threadId);

    MedicalChatThreadResponseDto closeThread(Long threadId);

    MedicalChatMessageResponseDto sendMessageToThread(Long threadId, MedicalChatMessageRequestDto requestDto);

    MedicalChatMessageResponseDto uploadImagesToThread(Long threadId,
                                                       MultipartFile[] files,
                                                       String message,
                                                       boolean emergency);

    List<DoctorPetSearchResponseDto> searchPetsForDoctor(String query);

    DoctorPetHistoryResponseDto getPetHistoryForDoctor(Long petId);

    PetMedicalRespnseDto createDiagnosisFromHistory(Long petId, PetMedicalRequestDto petMedicalRequestDto);
}
