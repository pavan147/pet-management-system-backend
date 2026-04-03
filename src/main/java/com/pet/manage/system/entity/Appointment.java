// src/main/java/com/example/entity/Appointment.java
package com.pet.manage.system.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "appointments")
@Data
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String phone;
    private String date; // Store as String or LocalDate depending on your needs
    private String time;
    private String reason;

    private String status; // waiting, checked-in, pending
    private String action; // check-in, not-present, recall, etc.
}