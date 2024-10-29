package com.ecommerce.service;

import com.ecommerce.domain.dto.SignupDTO;
import com.ecommerce.domain.dto.SigninDTO;
import com.ecommerce.domain.dto.AuthResponseDTO;
import com.ecommerce.domain.model.User;
import com.ecommerce.domain.model.UserProfile;
import com.ecommerce.domain.repository.UserRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.mindrot.jbcrypt.BCrypt;
import java.time.LocalDateTime;

@ApplicationScoped
public class AuthService {
    
    @Inject
    UserRepository userRepository;
    
    @Inject
    JwtService jwtService;
    
    @Inject
    UserProfileService userProfileService;
    
    @Inject
    EmailService emailService;
    
    public Uni<AuthResponseDTO> signup(SignupDTO signupDTO) {
        return userRepository.existsByEmail(signupDTO.getEmail())
            .chain(exists -> {
                if (exists) {
                    return Uni.createFrom().failure(
                        new IllegalStateException("Email already registered"));
                }
                
                User user = new User();
                user.setEmail(signupDTO.getEmail());
                user.setPassword(BCrypt.hashpw(signupDTO.getPassword(), BCrypt.gensalt()));
                user.setPhoneNumber(signupDTO.getPhoneNumber());
                user.setEmailVerified(false);
                user.setPhoneVerified(false);
                
                UserProfile profile = new UserProfile();
                profile.setUser(user);
                profile.setFirstName(signupDTO.getFirstName());
                profile.setLastName(signupDTO.getLastName());
                user.setProfile(profile);
                
                return userRepository.persist(user)
                    .chain(savedUser -> {
                        String token = jwtService.generateToken(savedUser);
                        UserDTO userDTO = userProfileService.mapToDTO(savedUser);
                        
                        // Send verification email asynchronously
                        emailService.sendVerificationEmail(savedUser)
                            .subscribe().with(
                                success -> {},
                                error -> System.err.println("Failed to send verification email: " + error)
                            );
                        
                        return Uni.createFrom().item(
                            new AuthResponseDTO(token, userDTO));
                    });
            });
    }
    
    public Uni<AuthResponseDTO> signin(SigninDTO signinDTO) {
        return userRepository.findByEmail(signinDTO.getEmail())
            .chain(user -> {
                if (user == null || !BCrypt.checkpw(signinDTO.getPassword(), user.getPassword())) {
                    return Uni.createFrom().failure(
                        new SecurityException("Invalid credentials"));
                }
                
                String token = jwtService.generateToken(user);
                UserDTO userDTO = userProfileService.mapToDTO(user);
                return Uni.createFrom().item(new AuthResponseDTO(token, userDTO));
            });
    }
    
    public Uni<Boolean> verifyEmail(String token) {
        return userRepository.find("verificationToken", token)
            .firstResult()
            .chain(user -> {
                if (user == null) {
                    return Uni.createFrom().item(false);
                }
                
                if (LocalDateTime.now().isAfter(user.getVerificationTokenExpiry())) {
                    return Uni.createFrom().item(false);
                }
                
                user.setEmailVerified(true);
                user.setVerificationToken(null);
                user.setVerificationTokenExpiry(null);
                
                return userRepository.persist(user)
                    .map(savedUser -> true);
            });
    }
}