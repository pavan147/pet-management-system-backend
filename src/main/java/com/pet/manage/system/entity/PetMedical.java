package com.pet.manage.system.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pet_medical")
@Data
@Builder
public class PetMedical {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long petMedicalId;

    private String allergies;
    private String diagnosis;
    private String treatmentSuggestions;
    private LocalDate validateTill;

    @ManyToOne
    @JoinColumn(name = "pet_id")
    private Pet pet;

    @OneToMany(mappedBy = "petMedical", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Prescription> prescriptions = new ArrayList<>();
}