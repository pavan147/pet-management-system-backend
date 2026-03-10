package com.pet.manage.system.dtos;

import lombok.Data;

import java.util.List;

@Data
public class PrescriptionDTO {
    private String medicine;
    private String dosage;
    private Integer frequency;
    private Integer duration;
    private String instructions;
    private List<String> times;
    private String meal;

}
