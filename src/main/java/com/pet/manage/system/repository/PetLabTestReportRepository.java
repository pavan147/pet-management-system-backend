package com.pet.manage.system.repository;

import com.pet.manage.system.entity.PetLabTestReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PetLabTestReportRepository extends JpaRepository<PetLabTestReport, Long> {

    List<PetLabTestReport> findByPetIdOrderByUploadedAtDescIdDesc(Long petId);

    List<PetLabTestReport> findByPetIdInOrderByUploadedAtDescIdDesc(List<Long> petIds);
}

