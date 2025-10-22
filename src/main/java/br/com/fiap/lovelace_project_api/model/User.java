package br.com.fiap.lovelace_project_api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {
    
    @Id
    private String id;
    
    @Indexed(unique = true)
    private String username;
    
    @Indexed(unique = true)
    private String email;

    private boolean emailVerified;

    private String emailVerificationToken;
    
    private LocalDateTime emailVerificationTokenExpiry;
    
    private String password;
    
    private String passwordResetToken;
    
    private LocalDateTime passwordResetTokenExpiry;
    
    private LocalDateTime passwordChangedAt;
    
    @Builder.Default
    private Set<String> roles = new HashSet<>();
    
    private boolean enabled;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
}
