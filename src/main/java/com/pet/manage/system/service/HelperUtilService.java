package com.pet.manage.system.service;

import com.pet.manage.system.entity.Owner;
import com.pet.manage.system.entity.PetMedical;


public interface HelperUtilService {

    Owner findOwnerByContact(String contact);

    byte[] generatePrescriptionPdf(PetMedical record);


}
