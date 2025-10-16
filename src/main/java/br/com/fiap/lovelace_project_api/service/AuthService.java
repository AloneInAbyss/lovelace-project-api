package br.com.fiap.lovelace_project_api.service;

import br.com.fiap.lovelace_project_api.dto.AuthResponse;
import br.com.fiap.lovelace_project_api.dto.LoginRequest;
import br.com.fiap.lovelace_project_api.dto.RefreshTokenRequest;
import br.com.fiap.lovelace_project_api.dto.RegisterRequest;
import br.com.fiap.lovelace_project_api.exception.EmailNotVerifiedException;
import br.com.fiap.lovelace_project_api.model.User;
import br.com.fiap.lovelace_project_api.model.VerificationToken;
import br.com.fiap.lovelace_project_api.repository.UserRepository;
import br.com.fiap.lovelace_project_api.repository.VerificationTokenRepository;
import br.com.fiap.lovelace_project_api.security.JwtTokenProvider;
import br.com.fiap.lovelace_project_api.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;
    private final EmailService emailService;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username is already taken");
        }
        
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already in use");
        }
        
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(Set.of("ROLE_USER"))
                .enabled(false) // User must verify email to enable account
                .emailVerified(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        User savedUser = userRepository.save(user);

        VerificationToken verificationToken = createNewVerificationToken(savedUser);

        verificationTokenRepository.save(verificationToken);

        emailService.sendVerificationEmail(savedUser.getEmail(), verificationToken.getToken());

        return AuthResponse.builder()
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .message("User registered successfully. Please check your email for verification instructions.")
                .build();
    }

    private VerificationToken createNewVerificationToken(User user) {
        String token = UUID.randomUUID().toString();
        return VerificationToken.builder()
                .token(token)
                .userId(user.getId())
                .email(user.getEmail())
                .expiryDate(LocalDateTime.now().plusHours(24))
                .used(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public void verifyEmail(String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid verification token"));

        if (verificationToken.isUsed()) {
            throw new RuntimeException("Verification token has already been used");
        }

        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Verification token has expired");
        }

        User user = userRepository.findById(verificationToken.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setEmailVerified(true);
        user.setEnabled(true);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        verificationToken.setUsed(true);
        verificationTokenRepository.save(verificationToken);

        emailService.sendWelcomeEmail(user.getEmail(), user.getUsername());
    }

    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (user.isEmailVerified()) {
            throw new RuntimeException("Email is already verified");
        }
        
        verificationTokenRepository.deleteByUserId(user.getId());
        
        VerificationToken verificationToken = createNewVerificationToken(user);
        
        verificationTokenRepository.save(verificationToken);
        
        emailService.sendVerificationEmail(user.getEmail(), verificationToken.getToken());
    }
    
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        if (!user.isEmailVerified()) {
            Optional<VerificationToken> recentToken = verificationTokenRepository
                    .findByUserId(user.getId())
                    .filter(token -> !token.isUsed())
                    .filter(token -> {
                        long minutesSinceCreation = ChronoUnit.MINUTES.between(
                            token.getCreatedAt(), 
                            LocalDateTime.now()
                        );
                        return minutesSinceCreation < 5; // Within last 5 minutes
                    });
            
            if (recentToken.isEmpty()) {
                resendVerificationEmail(user.getEmail());
                throw new EmailNotVerifiedException(
                    "Email not verified. A new verification email has been sent to you."
                );
            } else {
                throw new EmailNotVerifiedException(
                    "Email not verified. Please check your inbox for the verification email. " +
                    "If you didn't receive it, you can request a new one in a few minutes."
                );
            }
        }
        
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String jwt = jwtTokenProvider.generateToken(userPrincipal);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userPrincipal);
        
        return AuthResponse.builder()
                .token(jwt)
                .refreshToken(refreshToken)
                .username(userPrincipal.getUsername())
                .email(userPrincipal.getEmail())
                .roles(userPrincipal.getAuthorities().stream()
                        .map(Object::toString)
                        .collect(java.util.stream.Collectors.toSet()))
                .build();
    }
    
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        
        // Extract username from refresh token
        String username = jwtTokenProvider.extractUsername(refreshToken);
        
        // Load user details
        UserPrincipal userPrincipal = (UserPrincipal) userDetailsService.loadUserByUsername(username);
        
        // Validate the refresh token
        if (!jwtTokenProvider.validateToken(refreshToken, userPrincipal)) {
            throw new RuntimeException("Invalid or expired refresh token");
        }
        
        // Generate new access token and refresh token
        String newAccessToken = jwtTokenProvider.generateToken(userPrincipal);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(userPrincipal);
        
        return AuthResponse.builder()
                .token(newAccessToken)
                .refreshToken(newRefreshToken)
                .username(userPrincipal.getUsername())
                .email(userPrincipal.getEmail())
                .roles(userPrincipal.getAuthorities().stream()
                        .map(Object::toString)
                        .collect(java.util.stream.Collectors.toSet()))
                .build();
    }
}
