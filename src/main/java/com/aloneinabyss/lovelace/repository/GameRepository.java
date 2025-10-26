package com.aloneinabyss.lovelace.repository;

import com.aloneinabyss.lovelace.model.Game;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface GameRepository extends MongoRepository<Game, String> {
    
    Optional<Game> findByIdAndExpiresAtAfter(String id, LocalDateTime currentTime);
    
    List<Game> findByIdIn(List<String> ids);
    
    void deleteByExpiresAtBefore(LocalDateTime currentTime);
    
}
