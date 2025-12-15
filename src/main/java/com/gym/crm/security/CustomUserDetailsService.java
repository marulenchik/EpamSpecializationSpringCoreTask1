package com.gym.crm.security;

import com.gym.crm.model.User;
import com.gym.crm.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;

/**
 * Custom UserDetailsService for Spring Security
 */
@Service
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final BruteForceProtectionService bruteForceProtectionService;

    public CustomUserDetailsService(UserRepository userRepository, 
                                   BruteForceProtectionService bruteForceProtectionService) {
        this.userRepository = userRepository;
        this.bruteForceProtectionService = bruteForceProtectionService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user by username: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // Check if account is locked
        if (bruteForceProtectionService.isAccountLocked(username)) {
            LocalDateTime lockoutTime = bruteForceProtectionService.getAccountLockoutTime(username);
            log.warn("Account is locked for user: {} until {}", username, lockoutTime);
            throw new RuntimeException("Account is temporarily locked due to multiple failed login attempts");
        }

        // Determine user type for role assignment
        String userType = determineUserType(user);
        String role = "ROLE_" + userType.toUpperCase();

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword()) // This will be the hashed password
                .authorities(Collections.singletonList(new SimpleGrantedAuthority(role)))
                .accountExpired(false)
                .accountLocked(!user.getIsActive())
                .credentialsExpired(false)
                .disabled(!user.getIsActive())
                .build();
    }

    private String determineUserType(User user) {
        // Check if user is a trainee or trainer based on class type
        String className = user.getClass().getSimpleName();
        if ("Trainee".equals(className)) {
            return "TRAINEE";
        } else if ("Trainer".equals(className)) {
            return "TRAINER";
        }
        return "USER";
    }
}



