package com.pet.manage.system.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pet")
@Data
public class Pet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String petName;
    private String petType;
    private String otherPetType;
    private String breed;
    private LocalDate registrationDate = LocalDate.now();
    private LocalDate dob;
    private String description;
    private String gender;
    private String allergies;
    @Lob
    @Column(name = "photo" , columnDefinition = "MEDIUMBLOB")
    private byte[] photo;

    @Column(name = "photo_content_type")
    private String photoContentType;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private Owner owner;

    @OneToMany(mappedBy = "pet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PetVaccinationRecord> vaccinationRecords = new ArrayList<>();

    @OneToMany(mappedBy = "pet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PetMedical> petMedicals= new ArrayList<>();
}