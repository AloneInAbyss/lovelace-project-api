package com.aloneinabyss.lovelace.pages.games.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.aloneinabyss.lovelace.pages.games.model.GameDetails;

@Repository
public interface GameDetailsRepository extends MongoRepository<GameDetails, String> {
    
}
