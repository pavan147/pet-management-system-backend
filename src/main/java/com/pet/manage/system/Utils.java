package com.pet.manage.system;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pet.manage.system.dtos.OwnerResponseDto;
import com.pet.manage.system.dtos.PetResponseDto;
import com.pet.manage.system.entity.Owner;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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