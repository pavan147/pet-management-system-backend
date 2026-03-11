package com.pet.manage.system.repository;

import com.pet.manage.system.entity.Owner;
import com.pet.manage.system.entity.PetMedical;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PetMedicalRepository extends JpaRepository<PetMedical, Long> {
}
