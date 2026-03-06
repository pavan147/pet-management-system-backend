package com.pet.manage.system.repository;


import com.pet.manage.system.entity.Owner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OwnerRepository extends JpaRepository<Owner, Long> {

    Optional<Owner> findByEmail(String email);
    Optional<Owner> findByPhoneNumber(String phoneNumber);
}