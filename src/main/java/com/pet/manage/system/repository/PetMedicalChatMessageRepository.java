package com.pet.manage.system.repository;

import com.pet.manage.system.entity.PetMedicalChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PetMedicalChatMessageRepository extends JpaRepository<PetMedicalChatMessage, Long> {

    List<PetMedicalChatMessage> findByPetIdOrderByCreatedAtAsc(Long petId);

    List<PetMedicalChatMessage> findByThreadIdOrderByCreatedAtAsc(Long threadId);

    List<PetMedicalChatMessage> findByPetIdAndThreadIsNullOrderByCreatedAtAsc(Long petId);

    java.util.Optional<PetMedicalChatMessage> findTopByPetIdOrderByCreatedAtDesc(Long petId);

    java.util.Optional<PetMedicalChatMessage> findTopByThreadIdOrderByCreatedAtDesc(Long threadId);

    boolean existsByPetIdAndEmergencyTrue(Long petId);

    boolean existsByThreadIdAndEmergencyTrue(Long threadId);

    List<PetMedicalChatMessage> findTop20ByEmergencyTrueOrderByCreatedAtDesc();

    long countByThreadId(Long threadId);
}

