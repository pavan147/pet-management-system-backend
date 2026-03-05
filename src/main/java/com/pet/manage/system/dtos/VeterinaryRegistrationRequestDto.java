package com.pet.manage.system.dtos;

import com.pet.manage.system.global.validations.OtherPetTypeRequired;
import com.pet.manage.system.global.validations.PasswordMatches;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
@Data
@PasswordMatches
@OtherPetTypeRequired
public class VeterinaryRegistrationRequestDto {

    @NotBlank(message = "Owner name is required.")
    private String ownerName;

    @Email(message = "Please provide a valid email address.")
    @NotBlank(message = "Email is required.")
    private String email;

    @NotBlank(message = "Phone number is required.")
    private String phoneNumber;

    @NotBlank(message = "Password is required.")
    private String password;

    @NotBlank(message = "Please confirm your password.")
    private String confirmPassword;

    @NotBlank(message = "Pet name is required.")
    private String petName;

    @NotBlank(message = "Pet type is required.")
    private String petType;

    private String otherPetType;

    @NotBlank(message = "Breed is required.")
    private String breed;

    @NotBlank(message = "Address is required.")
    private String address;

    @NotNull(message = "Registration date is required.")
    private LocalDate registrationDate;

    // Getters and setters (or use Lombok @Data if configured)
    // ...
}