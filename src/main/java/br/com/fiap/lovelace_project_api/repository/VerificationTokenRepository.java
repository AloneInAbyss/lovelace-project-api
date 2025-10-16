package br.com.fiap.lovelace_project_api.repository;

import br.com.fiap.lovelace_project_api.model.VerificationToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends MongoRepository<VerificationToken, String> {

    Optional<VerificationToken> findByToken(String token);

    Optional<VerificationToken> findByUserId(String userId);

    void deleteByUserId(String userId);
    
}
