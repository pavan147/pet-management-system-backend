package com.pet.manage.system.commons;

public class Constants {

     public static final String[] PUBLIC_URLS = {
            "/api/pets/book-appointment/**" ,
             "/api/auth/login",
             "/api/otp/**"
    };

     public static final String ADMIN_ROLE = "ROLE_ADMIN";
     public static final String PET_OWNER_ROLE = "ROLE_PET_OWNER";
     public static final String RECEPTIONIST_ROLE = "ROLE_RECEPTIONIST";
     public static final String DOCTOR_ROLE = "ROLE_DOCTOR";
}
