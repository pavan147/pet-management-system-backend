package com.pet.manage.system.service;

import com.pet.manage.system.entity.Owner;
import org.springframework.stereotype.Service;


public interface HelperUtilService {

    Owner findOwnerByContact(String contact);
}
