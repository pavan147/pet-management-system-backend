package com.pet.manage.system;

import com.pet.manage.system.dtos.OwnerResponseDto;
import com.pet.manage.system.dtos.PetResponseDto;
import com.pet.manage.system.entity.Owner;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class Utils {

    // Static utility method: pass ModelMapper as a parameter
    public static OwnerResponseDto getOwnerDetails(Owner owner, ModelMapper modelMapper) {
        // Map Owner to OwnerResponseDto
        OwnerResponseDto ownerResponseDto = modelMapper.map(owner, OwnerResponseDto.class);

        // Map List<Pet> to List<PetResponseDto>
        Type listType = new TypeToken<List<PetResponseDto>>() {
        }.getType();
        List<PetResponseDto> petResponseDto = modelMapper.map(owner.getPets(), listType);

        // Set pets in OwnerResponseDto
        ownerResponseDto.setPets(petResponseDto);

        return ownerResponseDto;
    }
}