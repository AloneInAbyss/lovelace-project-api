package com.aloneinabyss.lovelace.pages.games.service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.aloneinabyss.lovelace.pages.games.dto.GameSearchResponse;
import com.aloneinabyss.lovelace.pages.games.dto.LowestPriceListing;
import com.aloneinabyss.lovelace.pages.games.model.GameDetails;
import com.aloneinabyss.lovelace.pages.games.model.GameListing;
import com.aloneinabyss.lovelace.pages.games.repository.GameDetailsRepository;
import com.aloneinabyss.lovelace.pages.games.repository.GameListingRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameService {

    private final GameDetailsRepository gameDetailsRepository;
    private final GameListingRepository gameListingRepository;

    public Page<GameSearchResponse> searchGames(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        
        // Search games by name (case-insensitive, partial match)
        Page<GameDetails> gamesPage = gameDetailsRepository.findByNameContainingIgnoreCase(query, pageable);
        
        // Map to response DTOs with lowest prices
        return gamesPage.map(this::mapToSearchResponse);
    }

    public GameSearchResponse getGameById(String gameId) {
        GameDetails game = gameDetailsRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found with id: " + gameId));
        
        return mapToSearchResponse(game);
    }

    private GameSearchResponse mapToSearchResponse(GameDetails game) {
        // Get all listings for this game
        List<GameListing> listings = gameListingRepository.findByGameId(game.getId());
        
        // Group by condition and find lowest price for each
        Map<String, LowestPriceListing> lowestPricesByCondition = listings.stream()
                .collect(Collectors.groupingBy(
                        GameListing::getCondition,
                        Collectors.collectingAndThen(
                                Collectors.minBy(Comparator.comparing(GameListing::getPrice)),
                                optional -> optional.map(this::mapToLowestPriceListing).orElse(null)
                        )
                ));

        return GameSearchResponse.builder()
                .id(game.getId())
                .name(game.getName())
                .yearPublished(game.getYearPublished())
                .isExpansion(game.getIsExpansion())
                .lowestPricesByCondition(lowestPricesByCondition)
                .build();
    }

    private LowestPriceListing mapToLowestPriceListing(GameListing listing) {
        return LowestPriceListing.builder()
                .listingId(listing.getListingId())
                .condition(listing.getCondition())
                .price(listing.getPrice())
                .city(listing.getCity())
                .state(listing.getState())
                .listingUrl(listing.getListingUrl())
                .build();
    }
}
