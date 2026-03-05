//package com.pet.manage.system.global.validations;
//
//import jakarta.validation.Constraint;
//import jakarta.validation.Payload;
//
//import java.lang.annotation.*;
//
//@Documented
//@Constraint(validatedBy = OtherPetTypeRequiredValidator.class)
//@Target({ ElementType.TYPE })
//@Retention(RetentionPolicy.RUNTIME)
//public @interface OtherPetTypeRequired {
//    String message() default "If pet type is 'other', 'otherPetType' is required.";
//    Class<?>[] groups() default {};
//    Class<? extends Payload>[] payload() default {};
//}