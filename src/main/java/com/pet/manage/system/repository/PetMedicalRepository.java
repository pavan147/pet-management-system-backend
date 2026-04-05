package com.pet.manage.system.repository;

import com.pet.manage.system.entity.PetMedical;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PetMedicalRepository extends JpaRepository<PetMedical, Long> {

    List<PetMedical> findByPetIdIn(List<Long> petIds);

    @Query("""
            select distinct pm
            from PetMedical pm
            left join fetch pm.prescriptions
            left join fetch pm.pet p
            left join fetch p.owner
            where pm.petMedicalId = :petMedicalId
              and p.id = :petId
            """)
    Optional<PetMedical> findForPdfByPetIdAndMedicalId(@Param("petId") Long petId, @Param("petMedicalId") Long petMedicalId);
}
