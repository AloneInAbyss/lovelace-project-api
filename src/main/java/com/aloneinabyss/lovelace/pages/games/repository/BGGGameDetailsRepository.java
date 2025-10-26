package com.aloneinabyss.lovelace.pages.games.repository;

import com.aloneinabyss.lovelace.pages.games.model.BGGGameDetails;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BGGGameDetailsRepository extends MongoRepository<BGGGameDetails, String> {
    
    Optional<BGGGameDetails> findById(Long id);
    
    Optional<BGGGameDetails> findByName(String name);
    
    boolean existsById(Long id);
    
}
