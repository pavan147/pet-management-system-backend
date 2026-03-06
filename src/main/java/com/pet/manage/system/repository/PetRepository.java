package com.pet.manage.system.repository;

import com.pet.manage.system.entity.Pet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PetRepository extends JpaRepository<Pet, Long> {}