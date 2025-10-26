package com.aloneinabyss.lovelace.pages.games.controller;

import com.aloneinabyss.lovelace.pages.games.model.GameDetails;
import com.aloneinabyss.lovelace.pages.games.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
public class GameController {
    
    private final GameService gameService;
    
    @GetMapping
    public ResponseEntity<List<GameDetails>> getAllGames() {
        List<GameDetails> games = gameService.getAllGames();
        return ResponseEntity.ok(games);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<GameDetails> getGameById(@PathVariable String id) {
        GameDetails game = gameService.getGameById(id);
        return ResponseEntity.ok(game);
    }
    
    @PostMapping
    public ResponseEntity<GameDetails> createGame(@RequestBody GameDetails gameDetails) {
        GameDetails createdGame = gameService.createGame(gameDetails);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdGame);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<GameDetails> updateGame(
            @PathVariable String id,
            @RequestBody GameDetails gameDetails) {
        GameDetails updatedGame = gameService.updateGame(id, gameDetails);
        return ResponseEntity.ok(updatedGame);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGame(@PathVariable String id) {
        gameService.deleteGame(id);
        return ResponseEntity.noContent().build();
    }
    
}
