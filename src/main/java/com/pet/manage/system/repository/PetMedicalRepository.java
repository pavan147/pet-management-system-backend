package com.pet.manage.system.repository;

import com.pet.manage.system.entity.PetMedical;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PetMedicalRepository extends JpaRepository<PetMedical, Long> {

    List<PetMedical> findByPetIdIn(List<Long> petIds);
}
