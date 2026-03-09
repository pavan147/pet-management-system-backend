package com.pet.manage.system.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "pet_vaccination_record")
@Data
public class PetVaccinationRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many vaccination records can belong to one pet
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    @Column(nullable = false)
    private String vaccination;
    // e.g., Rabies, Distemper, etc.
    @Column(nullable = false)
    private String vaccineName; // e.g., Rabies Vaccine, Distemper Vaccine, etc.

    @Column(name = "brand_and_doses", nullable = false)
    private String brandAndDoses;

    @Column(name = "vaccination_date", nullable = false)
    private LocalDate vaccinationDate;

    @Column(name = "duration_months", nullable = false)
    private Integer durationMonths;

    @Column(name = "valid_till", nullable = false)
    private LocalDate validTill;

    @Column(nullable = false)
    private Integer weight;
}