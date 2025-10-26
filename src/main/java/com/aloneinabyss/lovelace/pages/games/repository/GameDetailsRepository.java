package com.aloneinabyss.lovelace.pages.games.repository;

import com.aloneinabyss.lovelace.pages.games.model.GameDetails;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GameDetailsRepository extends MongoRepository<GameDetails, String> {
    
    Optional<GameDetails> findByTitle(String title);
    
}
