package com.techconsulting.pfmp.controller;


import com.techconsulting.pfmp.domain.User;
import com.techconsulting.pfmp.model.UserProfile;
import com.techconsulting.pfmp.repository.UserRepository;
import com.techconsulting.pfmp.security.services.UserDetailsImpl;
import com.techconsulting.pfmp.utility.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/profile")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ProfileController {
    @Autowired
    UserRepository userRepository;

    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public UserProfile getProfile() {
        log.debug("Fetching user profile");
        try {
            UserDetailsImpl userDetails = SecurityUtil.getLoggedInUser();
            log.debug("Retrieved user details for: {}", userDetails.getId());
            UserProfile userProfile = new UserProfile();
            Optional<User> user = userRepository.findById(userDetails.getId());
            if (user.isPresent()){
                userProfile.setEmail(user.get().getEmail());
                userProfile.setFullName(user.get().getName());
                log.debug("User profile loaded successfully");
            } else {
                log.warn("User not found with ID: {}", userDetails.getId());
            }
            return userProfile;
        } catch (Exception e) {
            log.error("Error fetching user profile", e);
            throw e;
        }
    }

    @PutMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public UserProfile updateProfile(@RequestBody UserProfile updatedProfile) {
        log.debug("Updating user profile");
        try {
            UserDetailsImpl userDetails = SecurityUtil.getLoggedInUser();
            log.debug("Updating profile for user: {}", userDetails.getId());
            Optional<User> user = userRepository.findById(userDetails.getId());
            if (user.isPresent()){
                user.get().setEmail(updatedProfile.getEmail());
                user.get().setName(updatedProfile.getFullName());
                userRepository.save(user.get());
                log.info("User profile updated successfully");
            } else {
                log.warn("User not found with ID: {}", userDetails.getId());
            }
            return updatedProfile;
        } catch (Exception e) {
            log.error("Error updating user profile", e);
            throw e;
        }
    }
}

