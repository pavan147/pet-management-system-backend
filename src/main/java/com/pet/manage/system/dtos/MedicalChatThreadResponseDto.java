package com.pet.manage.system.dtos;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class MedicalChatThreadResponseDto {

    private Long threadId;
    private Long petId;
    private String petName;
    private Long ownerId;
    private String ownerName;
    private Long assignedVetId;
    private String assignedVetName;
    private String chatStatus;
    private LocalDateTime closedAt;
    private String closedByName;
    private boolean canClose;
    private boolean canReply;
    private MedicalChatPetContextDto petContext;
    private List<PetVaccinationRecorResponsedDTO> vaccinationRecords;
    private List<PetVaccinationRecorResponsedDTO> dewormingRecords;
    private List<PetMedicalRespnseDto> medicalRecords;
    private List<MedicalChatMessageResponseDto> messages;
}

