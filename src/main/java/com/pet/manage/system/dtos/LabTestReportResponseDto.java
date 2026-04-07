package com.pet.manage.system.dtos;

import com.pet.manage.system.entity.LabTestReportStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LabTestReportResponseDto {
    private Long id;
    private Long petId;
    private String petName;
    private Long ownerId;
    private String ownerName;
    private String ownerPhoneNumber;
    private String assignedVetName;
    private String title;
    private String labTestType;
    private String ownerNotes;
    private LabTestReportStatus status;
    private String reviewSummary;
    private String doctorReviewNotes;
    private LocalDate recommendedFollowUpDate;
    private LocalDate uploadedAt;
    private LocalDateTime reviewedAt;
    private String reviewedByName;
    private String originalFileName;
    private String contentType;
    private Long sizeBytes;
    private Long followUpMedicalRecordId;
    private boolean canReview;
    private boolean canDownload;
    private String downloadUrl;
}

