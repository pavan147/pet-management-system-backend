package com.pet.manage.system.controller;

import com.pet.manage.system.dtos.OwnerResponseDto;
import com.pet.manage.system.dtos.PetResponseDto;
import com.pet.manage.system.entity.Owner;
import com.pet.manage.system.service.OwnerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/owners")
@CrossOrigin(origins = "http://localhost:5173")
public class OwnerController {

    @Autowired
    private OwnerService ownerService;

    @GetMapping("/search")
    public ResponseEntity<OwnerResponseDto> getOwner(@RequestParam(required = false) String email,
                                                   @RequestParam(required = false) String phoneNumber) {
        return ResponseEntity.ok(ownerService.getOwnerByEmailOrPhone(email, phoneNumber));
    }
}