package com.pet.manage.system.repository;

import com.pet.manage.system.entity.PetMedicalChatThread;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PetMedicalChatThreadRepository extends JpaRepository<PetMedicalChatThread, Long> {

    List<PetMedicalChatThread> findByPetIdOrderByCreatedAtDesc(Long petId);

    Optional<PetMedicalChatThread> findTopByPetIdAndStatusOrderByCreatedAtDesc(Long petId, String status);

    boolean existsByPetIdAndStatus(Long petId, String status);
}

