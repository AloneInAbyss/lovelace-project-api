package com.aloneinabyss.lovelace.pages.games.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.aloneinabyss.lovelace.pages.games.model.Game;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface GameRepository extends MongoRepository<Game, String> {
    
    Optional<Game> findByIdAndExpiresAtAfter(String id, LocalDateTime currentTime);
    
    List<Game> findByIdIn(List<String> ids);
    
    void deleteByExpiresAtBefore(LocalDateTime currentTime);
    
}
