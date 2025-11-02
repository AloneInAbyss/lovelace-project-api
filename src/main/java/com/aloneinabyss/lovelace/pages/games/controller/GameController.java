package com.aloneinabyss.lovelace.pages.games.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aloneinabyss.lovelace.pages.games.dto.GameSearchResponse;
import com.aloneinabyss.lovelace.pages.games.service.GameService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    /**
     * Search for games by name with pagination.
     * Returns game details along with the lowest price listing for each condition (new, used, auction).
     * 
     * @param query Search query (game name)
     * @param page Page number (0-indexed)
     * @param size Page size
     * @return Page of game search results
     */
    @GetMapping("/search")
    public ResponseEntity<Page<GameSearchResponse>> searchGames(
            @RequestParam(required = false, defaultValue = "") String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("Searching games with query: '{}', page: {}, size: {}", query, page, size);
        
        Page<GameSearchResponse> results = gameService.searchGames(query, page, size);
        
        log.info("Found {} games matching query '{}'", results.getTotalElements(), query);
        
        return ResponseEntity.ok(results);
    }

    /**
     * Get game details by ID.
     * Retrieves detailed information about a specific game including the lowest price listing for each condition.
     * 
     * @param gameId Game ID
     * @return Game details with lowest prices
     */
    @GetMapping("/{gameId}")
    public ResponseEntity<GameSearchResponse> getGameById(@PathVariable String gameId) {
        log.info("Fetching game details for ID: {}", gameId);
        
        GameSearchResponse game = gameService.getGameById(gameId);
        
        return ResponseEntity.ok(game);
    }
}
