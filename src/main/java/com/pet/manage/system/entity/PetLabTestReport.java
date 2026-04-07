package com.pet.manage.system.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "pet_lab_test_report")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PetLabTestReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String labTestType;

    @Column(length = 4000)
    private String ownerNotes;

    @Enumerated(EnumType.STRING)
    private LabTestReportStatus status;

    private String reviewSummary;

    @Column(length = 4000)
    private String doctorReviewNotes;

    private LocalDate recommendedFollowUpDate;
    private LocalDate uploadedAt;
    private LocalDateTime reviewedAt;
    private String originalFileName;
    private String contentType;
    private Long sizeBytes;

    @Lob
    @Column(name = "report_data", columnDefinition = "LONGBLOB")
    private byte[] reportData;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id")
    private Pet pet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by_user_id")
    private Owner uploadedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_user_id")
    private Owner reviewedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follow_up_medical_id")
    private PetMedical followUpMedicalRecord;

    @PrePersist
    public void applyDefaults() {
        if (uploadedAt == null) {
            uploadedAt = LocalDate.now();
        }
        if (status == null) {
            status = LabTestReportStatus.PENDING_REVIEW;
        }
    }
}

