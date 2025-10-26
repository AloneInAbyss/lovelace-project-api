package com.aloneinabyss.lovelace.controller;

import com.aloneinabyss.lovelace.dto.GameDetailsResponse;
import com.aloneinabyss.lovelace.dto.GameListingsResponse;
import com.aloneinabyss.lovelace.dto.GameSearchResponse;
import com.aloneinabyss.lovelace.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
public class GameController {
    
    private final GameService gameService;
    
    @GetMapping("/search")
    public ResponseEntity<GameSearchResponse> searchGames(
            @RequestParam(required = false, defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        GameSearchResponse response = gameService.searchGames(q, page, size);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{gameId}")
    public ResponseEntity<GameDetailsResponse> getGameDetails(@PathVariable String gameId) {
        GameDetailsResponse response = gameService.getGameDetails(gameId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{gameId}/listings")
    public ResponseEntity<GameListingsResponse> getGameListings(
            @PathVariable String gameId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        GameListingsResponse response = gameService.getGameListings(gameId, page, size);
        return ResponseEntity.ok(response);
    }
    
}
