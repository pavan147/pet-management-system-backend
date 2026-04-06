package com.pet.manage.system.dtos;

import lombok.Data;

import java.util.List;

@Data
public class OwnerResponseDto {

    private Long id;
    private String ownerName;
    private String email;
    private String phoneNumber;
    private String password;
    private String address;
    private Boolean phoneVerified;
    private List<PetResponseDto> pets; //
}
