package com.techconsulting.pfmp.controller;


import com.techconsulting.pfmp.domain.Role;
import com.techconsulting.pfmp.domain.RoleName;
import com.techconsulting.pfmp.domain.User;
import com.techconsulting.pfmp.model.JwtRequest;
import com.techconsulting.pfmp.model.JwtResponse;
import com.techconsulting.pfmp.model.ResponseMessage;
import com.techconsulting.pfmp.model.SignUpForm;
import com.techconsulting.pfmp.repository.RoleRepository;
import com.techconsulting.pfmp.repository.UserRepository;
import com.techconsulting.pfmp.security.JwtHelper;
import com.techconsulting.pfmp.utility.Base64Util;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Set;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtHelper jwtHelper;

    @PostMapping("/signIn")
    public ResponseEntity<?> authenticateUser(@Valid @RequestHeader("Authorization") String authorization) {
        JwtRequest request = Base64Util.getUsernameAndPassword(authorization);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Extract user from authentication context
        String token = jwtHelper.generateJwtToken((org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal());

        return ResponseEntity.ok(JwtResponse.builder()
                .accessToken(token)
                .username(request.getUsername())
                .build());
    }

    @PostMapping("/signUp")
    public ResponseEntity<ResponseMessage> registerUser(@Valid @RequestBody SignUpForm signUpRequest) {
        log.debug("SignUp request received for username: {}", signUpRequest.getUsername());
        try {
            if (userRepository.existsByUsername(signUpRequest.getUsername())) {
                log.warn("Signup failed - Username already taken: {}", signUpRequest.getUsername());
                return ResponseEntity.badRequest().body(new ResponseMessage("Fail -> Username is already taken!"));
            }

            if (userRepository.existsByEmail(signUpRequest.getEmail())) {
                log.warn("Signup failed - Email already in use: {}", signUpRequest.getEmail());
                return ResponseEntity.badRequest().body(new ResponseMessage("Fail -> Email is already in use!"));
            }

            // Creating user's account
            User user = new User(signUpRequest.getName(), signUpRequest.getUsername(), signUpRequest.getEmail(),
                    passwordEncoder.encode(signUpRequest.getPassword()));
            log.debug("Creating user account for: {}", signUpRequest.getUsername());

            Set<Role> roles = new HashSet<>();
            for (String role : signUpRequest.getRole()) {
                Role userRole = roleRepository.findByName(RoleName.valueOf("ROLE_" + role.toUpperCase()))
                        .orElseThrow(() -> new RuntimeException("Fail! -> Cause: User Role not found."));
                roles.add(userRole);
            }
            log.debug("Assigned roles to user: {}", roles);

            user.setRoles(roles);
            userRepository.save(user);
            log.info("User registered successfully: {}", signUpRequest.getUsername());

            return ResponseEntity.ok(new ResponseMessage("User registered successfully!"));
        } catch (Exception e) {
            log.error("Error during user registration for: {}", signUpRequest.getUsername(), e);
            throw e;
        }
    }
}