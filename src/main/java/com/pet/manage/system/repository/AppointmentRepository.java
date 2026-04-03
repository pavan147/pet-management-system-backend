// src/main/java/com/example/repository/AppointmentRepository.java
package com.pet.manage.system.repository;


import com.pet.manage.system.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {


    Optional<Appointment> findByEmailAndDate(String email, String date);

    Optional<Appointment> findByEmailAndNameAndDate(String email, String name, String date);

    Optional<Appointment> findByEmailAndNameAndPhoneAndDate(String email, String name, String phone, String date);

    List<Appointment> findByDate(String date);
}