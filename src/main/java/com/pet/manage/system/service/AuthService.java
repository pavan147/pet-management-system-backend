package com.pet.manage.system.service;


import com.pet.manage.system.dtos.LoginDto;
import com.pet.manage.system.dtos.RegisterDto;
import com.pet.manage.system.dtos.response.JwtAuthResponse;

public interface AuthService {
   // String register(RegisterDto registerDto);

    JwtAuthResponse login(LoginDto loginDto);
}
