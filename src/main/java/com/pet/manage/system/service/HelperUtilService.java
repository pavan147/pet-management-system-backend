package com.pet.manage.system.service;

import com.pet.manage.system.entity.Owner;
import com.pet.manage.system.entity.Pet;
import org.springframework.stereotype.Service;


public interface HelperUtilService {

    Owner findOwnerByContact(String contact);


}
