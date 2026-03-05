package com.pet.manage.system.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "veterinary_registration")
@Data
public class VeterinaryRegistration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String ownerName;
    private String email;
    private String phoneNumber;
    private String password;
    private String petName;
    private String petType;
    private String otherPetType;
    private String breed;
    private String address;
    private LocalDate registrationDate;
}