package com.pet.manage.system.global.validations;

import com.pet.manage.system.dtos.VeterinaryRegistrationRequestDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class OtherPetTypeRequiredValidator implements ConstraintValidator<OtherPetTypeRequired, VeterinaryRegistrationRequestDto> {

    @Override
    public boolean isValid(VeterinaryRegistrationRequestDto dto, ConstraintValidatorContext context) {
        if (dto.getPetType() == null) {
            return true; // Let @NotBlank on petType handle null/blank
        }
        if ("other".equalsIgnoreCase(dto.getPetType())) {
            if (dto.getOtherPetType() == null || dto.getOtherPetType().trim().isEmpty()) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                        .addPropertyNode("otherPetType")
                        .addConstraintViolation();
                return false;
            }
        }
        return true;
    }
}