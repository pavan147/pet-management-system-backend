package com.pet.manage.system;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pet.manage.system.dtos.OwnerResponseDto;
import com.pet.manage.system.dtos.PetResponseDto;
import com.pet.manage.system.entity.Owner;
import com.pet.manage.system.entity.Pet;
import org.modelmapper.ModelMapper;

import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Utils {

    // Static utility method: pass ModelMapper as a parameter
    public static OwnerResponseDto getOwnerDetails(Owner owner, ModelMapper modelMapper) {
        // Map Owner to OwnerResponseDto
        OwnerResponseDto ownerResponseDto = modelMapper.map(owner, OwnerResponseDto.class);

        // Map List<Pet> to List<PetResponseDto>
        List<PetResponseDto> petResponseDto = owner.getPets().stream()
                .map(pet -> mapPetToResponseDto(pet, modelMapper))
                .collect(Collectors.toList());

        // Set pets in OwnerResponseDto
        ownerResponseDto.setPets(petResponseDto);

        return ownerResponseDto;
    }

    public static PetResponseDto mapPetToResponseDto(Pet pet, ModelMapper modelMapper) {
        PetResponseDto petResponseDto = modelMapper.map(pet, PetResponseDto.class);
        petResponseDto.setPhotoContentType(pet.getPhotoContentType());
        if (pet.getPhoto() != null && pet.getPhoto().length > 0) {
            petResponseDto.setPhotoBase64(Base64.getEncoder().encodeToString(pet.getPhoto()));
        }
        return petResponseDto;
    }


    public static String errorStringToJson(String errorString) throws Exception {
        Map<String, String> errorMap = new LinkedHashMap<>();
        if (errorString == null || errorString.trim().isEmpty()) return "{}";

        String[] pairs = errorString.split(";");
        for (String pair : pairs) {
            String[] keyValue = pair.split(":", 2);
            if (keyValue.length == 2) {
                String key = keyValue[0].trim();
                String value = keyValue[1].trim();
                if (!key.isEmpty() && !value.isEmpty()) {
                    errorMap.put(key, value);
                }
            }
        }
        // Convert map to JSON string
        return new ObjectMapper().writeValueAsString(errorMap);
    }
}