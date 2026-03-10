package com.pet.manage.system.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table(name = "prescription")
@Data
public class Prescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String medicine;
    private String dosage;
    private String frequency;
    private String duration;
    private String instructions;

    private String morning;
    private String afternoon;
    private String evening;
    private String night;

    private String meal;

    @ManyToOne
    @JoinColumn(name = "pet_medical_id")
    private PetMedical petMedicalRequest;


}