package com.pet.manage.system.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pet_medical")
@Data
public class PetMedical {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long petMedicalId;

    private String allergies;
    private String diagnosis;
    private String treatmentSuggestions;

    @ManyToOne
    @JoinColumn(name = "pet_id")
    private Pet pet;

    @OneToMany(mappedBy = "petMedicalRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Prescription> prescriptions = new ArrayList<>();
}