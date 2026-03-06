package com.pet.manage.system.controller;

import com.pet.manage.system.entity.Owner;
import com.pet.manage.system.service.OwnerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/owners")
public class OwnerController {

    @Autowired
    private OwnerService ownerService;

    @GetMapping("/search")
    public Owner getOwner(@RequestParam(required = false) String email,
                          @RequestParam(required = false) String phoneNumber) {
        return ownerService.getOwnerByEmailOrPhone(email, phoneNumber);
    }
}