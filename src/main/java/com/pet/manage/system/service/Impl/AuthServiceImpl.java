package com.pet.manage.system.service.Impl;

import com.pet.manage.system.dtos.LoginDto;
import com.pet.manage.system.dtos.RegisterDto;
import com.pet.manage.system.dtos.response.JwtAuthResponse;
import com.pet.manage.system.entity.Owner;
import com.pet.manage.system.entity.Role;
import com.pet.manage.system.global.exception.TodoAPIException;
import com.pet.manage.system.repository.OwnerRepository;
import com.pet.manage.system.repository.RoleRepository;
import com.pet.manage.system.security.JwtTokenProvider;
import com.pet.manage.system.service.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {

    private OwnerRepository userRepository;
    private RoleRepository roleRepository;
    private PasswordEncoder passwordEncoder;
    private AuthenticationManager authenticationManager;
    private JwtTokenProvider jwtTokenProvider;

//    @Override
//    public String register(RegisterDto registerDto) {
//
//        // check username is already exists in database
//        if(userRepository.existsByUsername(registerDto.getUsername())){
//            throw new TodoAPIException(HttpStatus.BAD_REQUEST, "Username already exists!");
//        }
//
//        // check email is already exists in database
//        if(userRepository.existsByEmail(registerDto.getEmail())){
//            throw new TodoAPIException(HttpStatus.BAD_REQUEST, "Email is already exists!.");
//        }
//
//        User user = new User();
//        user.setName(registerDto.getName());
//        user.setUsername(registerDto.getUsername());
//        user.setEmail(registerDto.getEmail());
//        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
//
//        Set<Role> roles = new HashSet<>();
//        Role userRole = roleRepository.findByName("ROLE_USER");
//        roles.add(userRole);
//
//        user.setRoles(roles);
//
//        userRepository.save(user);
//
//        return "User Registered Successfully!.";
//    }

    @Override
    public JwtAuthResponse login(LoginDto loginDto) {

        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginDto.getUsernameOrEmail(),
                loginDto.getPassword()
        ));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtTokenProvider.generateToken(authentication);
        Optional<Owner> byUsernameOrEmail = userRepository.findByEmail(loginDto.getUsernameOrEmail());
          String role = null;
         if(byUsernameOrEmail.isPresent()){
             Owner loggUser = byUsernameOrEmail.get();
             Optional<Role> roleOptional = loggUser.getRoles().stream().findFirst();
             if(roleOptional.isPresent()){
                 Role userRole = roleOptional.get();
                 role = userRole.getName();
             }
         }
         JwtAuthResponse jwtAuthResponse = new JwtAuthResponse();
        jwtAuthResponse.setRole(role);
        jwtAuthResponse.setAccessToken(token);

        return jwtAuthResponse;
    }



}
