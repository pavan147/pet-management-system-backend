package com.pet.manage.system.repository;


import com.pet.manage.system.entity.VeterinaryRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VeterinaryRegistrationRepository extends JpaRepository<VeterinaryRegistration, Long> {
}