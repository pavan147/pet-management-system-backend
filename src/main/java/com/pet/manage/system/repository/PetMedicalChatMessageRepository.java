package com.pet.manage.system.repository;

import com.pet.manage.system.entity.PetMedicalChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PetMedicalChatMessageRepository extends JpaRepository<PetMedicalChatMessage, Long> {

    List<PetMedicalChatMessage> findByPetIdOrderByCreatedAtAsc(Long petId);

    java.util.Optional<PetMedicalChatMessage> findTopByPetIdOrderByCreatedAtDesc(Long petId);

    boolean existsByPetIdAndEmergencyTrue(Long petId);

    List<PetMedicalChatMessage> findTop20ByEmergencyTrueOrderByCreatedAtDesc();
}

