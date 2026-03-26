// src/main/java/com/example/repository/AppointmentRepository.java
package com.pet.manage.system.repository;


import com.pet.manage.system.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
}