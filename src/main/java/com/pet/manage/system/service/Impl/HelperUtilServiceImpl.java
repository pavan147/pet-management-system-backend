package com.pet.manage.system.service.Impl;

import com.pet.manage.system.entity.Owner;
import com.pet.manage.system.entity.Pet;
import com.pet.manage.system.repository.OwnerRepository;
import com.pet.manage.system.service.HelperUtilService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HelperUtilServiceImpl implements HelperUtilService {

    @Autowired
    private OwnerRepository ownerRepository;


    @Override
    public Owner findOwnerByContact(String contact) {
        Owner owner = ownerRepository.findByEmail(contact)
                .orElse(ownerRepository.findByPhoneNumber(contact)
                        .orElse(null));
        if (owner == null) {
            throw new RuntimeException("Owner not found with provided contact.");
        }
        return  owner;
    }


}
