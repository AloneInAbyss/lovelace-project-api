package br.com.fiap.lovelace_project_api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "verification_tokens")
public class VerificationToken {
    
    @Id
    private String id;
    
    @Indexed(unique = true)
    private String token;
    
    private String userId;
    
    private String email;
    
    private LocalDateTime expiryDate;
    
    private boolean used;
    
    private LocalDateTime createdAt;
    
}