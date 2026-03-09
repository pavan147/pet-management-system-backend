package com.pet.manage.system.repository;

import com.pet.manage.system.entity.PetVaccinationRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PetVaccinationRecordRepository extends JpaRepository<PetVaccinationRecord, Long> {
}