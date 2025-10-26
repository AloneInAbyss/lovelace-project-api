package com.aloneinabyss.lovelace.auth.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.aloneinabyss.lovelace.auth.model.User;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByPasswordResetToken(String passwordResetToken);
    
    Optional<User> findByEmailVerificationToken(String emailVerificationToken);
    
    Boolean existsByUsername(String username);
    
    Boolean existsByEmail(String email);
    
}
