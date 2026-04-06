package com.pet.manage.system.repository;

import com.pet.manage.system.entity.PetMedicalChatImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PetMedicalChatImageRepository extends JpaRepository<PetMedicalChatImage, Long> {

    List<PetMedicalChatImage> findByPetIdOrderByUploadedAtAsc(Long petId);
}

