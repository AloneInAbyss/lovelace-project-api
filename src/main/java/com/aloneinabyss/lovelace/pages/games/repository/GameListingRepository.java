package com.aloneinabyss.lovelace.pages.games.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.aloneinabyss.lovelace.pages.games.model.GameListing;

@Repository
public interface GameListingRepository extends MongoRepository<GameListing, String> {
    
    List<GameListing> findByGameId(String gameId);
    
    List<GameListing> findByCondition(String condition);
    
    List<GameListing> findByState(String state);
    
    List<GameListing> findByCity(String city);
}
