package com.aloneinabyss.lovelace.pages.games.service;

import com.aloneinabyss.lovelace.pages.games.model.GameDetails;
import com.aloneinabyss.lovelace.pages.games.repository.GameDetailsRepository;
import com.aloneinabyss.lovelace.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameService {
    
    private final GameDetailsRepository gameDetailsRepository;
    
    public List<GameDetails> getAllGames() {
        return gameDetailsRepository.findAll();
    }
    
    public GameDetails getGameById(String id) {
        return gameDetailsRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "GAME_NOT_FOUND",
                        "game.not.found",
                        id
                ));
    }
    
    public GameDetails createGame(GameDetails gameDetails) {
        gameDetails.setCreatedAt(LocalDateTime.now());
        gameDetails.setUpdatedAt(LocalDateTime.now());
        return gameDetailsRepository.save(gameDetails);
    }
    
    public GameDetails updateGame(String id, GameDetails gameDetails) {
        GameDetails existingGame = getGameById(id);
        existingGame.setTitle(gameDetails.getTitle());
        existingGame.setDescription(gameDetails.getDescription());
        existingGame.setGenre(gameDetails.getGenre());
        existingGame.setUpdatedAt(LocalDateTime.now());
        return gameDetailsRepository.save(existingGame);
    }
    
    public void deleteGame(String id) {
        GameDetails game = getGameById(id);
        gameDetailsRepository.delete(game);
    }
    
}
