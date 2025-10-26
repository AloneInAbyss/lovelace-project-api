package com.aloneinabyss.lovelace.service;

import com.aloneinabyss.lovelace.dto.GameDetailsResponse;
import com.aloneinabyss.lovelace.dto.GameListingDTO;
import com.aloneinabyss.lovelace.dto.GameListingsResponse;
import com.aloneinabyss.lovelace.dto.GameSearchResponse;
import com.aloneinabyss.lovelace.dto.GameSearchResultDTO;
import com.aloneinabyss.lovelace.model.Game;
import com.aloneinabyss.lovelace.repository.GameRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameService {
    
    private final GameRepository gameRepository;
    
    @Cacheable(value = "gameSearch", key = "#query + '-' + #page + '-' + #size")
    public GameSearchResponse searchGames(String query, int page, int size) {
        log.info("Searching games with query: {}, page: {}, size: {}", query, page, size);
        
        // TODO: Replace with actual external API call
        List<GameSearchResultDTO> mockResults = getMockSearchResults(query);
        
        // Apply pagination
        int totalResults = mockResults.size();
        int totalPages = (int) Math.ceil((double) totalResults / size);
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, totalResults);
        
        List<GameSearchResultDTO> paginatedResults = fromIndex < totalResults 
                ? mockResults.subList(fromIndex, toIndex) 
                : new ArrayList<>();
        
        return GameSearchResponse.builder()
                .results(paginatedResults)
                .currentPage(page)
                .totalPages(totalPages)
                .totalResults(totalResults)
                .pageSize(size)
                .build();
    }
    
    @Cacheable(value = "gameDetails", key = "#gameId")
    public GameDetailsResponse getGameDetails(String gameId) {
        log.info("Fetching game details for gameId: {}", gameId);
        
        // Check cache first
        Optional<Game> cachedGame = gameRepository.findByIdAndExpiresAtAfter(gameId, LocalDateTime.now());
        
        if (cachedGame.isPresent()) {
            log.info("Game {} found in cache", gameId);
            return mapGameToDetailsResponse(cachedGame.get());
        }
        
        // TODO: Replace with actual external API call
        Game game = fetchGameFromExternalAPI(gameId);
        
        // Cache the game
        game.setCachedAt(LocalDateTime.now());
        game.setExpiresAt(LocalDateTime.now().plusHours(24));
        gameRepository.save(game);
        
        return mapGameToDetailsResponse(game);
    }
    
    @Cacheable(value = "gameListings", key = "#gameId + '-' + #page + '-' + #size")
    public GameListingsResponse getGameListings(String gameId, int page, int size) {
        log.info("Fetching listings for gameId: {}, page: {}, size: {}", gameId, page, size);
        
        // TODO: Replace with actual external API call
        List<GameListingDTO> mockListings = getMockListings(gameId);
        
        // Apply pagination
        int totalListings = mockListings.size();
        int totalPages = (int) Math.ceil((double) totalListings / size);
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, totalListings);
        
        List<GameListingDTO> paginatedListings = fromIndex < totalListings 
                ? mockListings.subList(fromIndex, toIndex) 
                : new ArrayList<>();
        
        return GameListingsResponse.builder()
                .listings(paginatedListings)
                .currentPage(page)
                .totalPages(totalPages)
                .totalListings(totalListings)
                .pageSize(size)
                .build();
    }
    
    // TODO: Replace this method with actual external API integration
    private Game fetchGameFromExternalAPI(String gameId) {
        log.warn("Using mock data - replace with actual external API call");
        
        return Game.builder()
                .id(gameId)
                .name("Catan")
                .description("In Catan, players try to be the dominant force on the island of Catan by building settlements, cities, and roads.")
                .imageUrl("https://cf.geekdo-images.com/W3Bsga_uLP9kO91gZ7H8yw__imagepage/img/M_3Vg1j2HlNgkv7PL2xl2BJE2bw=/fit-in/900x600/filters:no_upscale():strip_icc()/pic2419375.jpg")
                .thumbnailUrl("https://cf.geekdo-images.com/W3Bsga_uLP9kO91gZ7H8yw__thumb/img/IzYEUm_gWFuRFOL8gQYqGm5gU6A=/fit-in/200x150/filters:strip_icc()/pic2419375.jpg")
                .yearPublished(1995)
                .minPlayers(3)
                .maxPlayers(4)
                .playingTime(90)
                .minAge(10)
                .categories(Arrays.asList("Negotiation", "Economic"))
                .mechanics(Arrays.asList("Dice Rolling", "Trading"))
                .designers(Arrays.asList("Klaus Teuber"))
                .artists(Arrays.asList("Volkan Baga", "Tanja Donner"))
                .publishers(Arrays.asList("Catan Studio", "999 Games"))
                .averageRating(7.2)
                .ratingsCount(95000)
                .build();
    }
    
    // TODO: Replace this method with actual external API call
    private List<GameSearchResultDTO> getMockSearchResults(String query) {
        log.warn("Using mock search results - replace with actual external API call");
        
        return Arrays.asList(
            GameSearchResultDTO.builder()
                    .id("1")
                    .name("Catan")
                    .thumbnailUrl("https://cf.geekdo-images.com/W3Bsga_uLP9kO91gZ7H8yw__thumb/img/IzYEUm_gWFuRFOL8gQYqGm5gU6A=/fit-in/200x150/filters:strip_icc()/pic2419375.jpg")
                    .yearPublished(1995)
                    .minPlayers(3)
                    .maxPlayers(4)
                    .playingTime(90)
                    .averageRating(7.2)
                    .ratingsCount(95000)
                    .categories(Arrays.asList("Negotiation", "Economic"))
                    .build(),
            GameSearchResultDTO.builder()
                    .id("2")
                    .name("Ticket to Ride")
                    .thumbnailUrl("https://cf.geekdo-images.com/ZWJg0dCdrWHxVnc0eFXK8w__thumb/img/OFjsoKQw7RqTTOgXMifUq4xOG7s=/fit-in/200x150/filters:strip_icc()/pic66668.jpg")
                    .yearPublished(2004)
                    .minPlayers(2)
                    .maxPlayers(5)
                    .playingTime(60)
                    .averageRating(7.4)
                    .ratingsCount(120000)
                    .categories(Arrays.asList("Trains"))
                    .build(),
            GameSearchResultDTO.builder()
                    .id("3")
                    .name("Pandemic")
                    .thumbnailUrl("https://cf.geekdo-images.com/S3ybV1LAp-8ya3TqNBfJzA__thumb/img/B7OYR5a-7J8y8NTOlZRw0Vw0HrU=/fit-in/200x150/filters:strip_icc()/pic1534148.jpg")
                    .yearPublished(2008)
                    .minPlayers(2)
                    .maxPlayers(4)
                    .playingTime(45)
                    .averageRating(7.6)
                    .ratingsCount(140000)
                    .categories(Arrays.asList("Medical", "Cooperative"))
                    .build(),
            GameSearchResultDTO.builder()
                    .id("4")
                    .name("Azul")
                    .thumbnailUrl("https://cf.geekdo-images.com/aPSHJO0d0XOpQR5X-wJonw__thumb/img/q4uWd2nMX1ZwpKN2xHzG6Of0L0E=/fit-in/200x150/filters:strip_icc()/pic6973671.png")
                    .yearPublished(2017)
                    .minPlayers(2)
                    .maxPlayers(4)
                    .playingTime(45)
                    .averageRating(7.8)
                    .ratingsCount(110000)
                    .categories(Arrays.asList("Abstract", "Tile Placement"))
                    .build(),
            GameSearchResultDTO.builder()
                    .id("5")
                    .name("7 Wonders")
                    .thumbnailUrl("https://cf.geekdo-images.com/35h9Za_JvMMMtx_92kT0Jg__thumb/img/kJFIF6V2vEhXbdR7IqLvYAq--0g=/fit-in/200x150/filters:strip_icc()/pic7149798.jpg")
                    .yearPublished(2010)
                    .minPlayers(2)
                    .maxPlayers(7)
                    .playingTime(30)
                    .averageRating(7.7)
                    .ratingsCount(130000)
                    .categories(Arrays.asList("Card Game", "Civilization"))
                    .build()
        );
    }
    
    // TODO: Replace this method with actual external API call
    private List<GameListingDTO> getMockListings(String gameId) {
        log.warn("Using mock listings - replace with actual external API call");
        
        return Arrays.asList(
            GameListingDTO.builder()
                    .id("listing1")
                    .gameId(gameId)
                    .sellerName("John's Board Games")
                    .price(new BigDecimal("35.00"))
                    .currency("USD")
                    .condition("Like New")
                    .location("New York, NY")
                    .description("Played twice, excellent condition")
                    .listedAt(LocalDateTime.now().minusDays(2))
                    .listingUrl("https://example.com/listing1")
                    .build(),
            GameListingDTO.builder()
                    .id("listing2")
                    .gameId(gameId)
                    .sellerName("GameStop Plus")
                    .price(new BigDecimal("28.50"))
                    .currency("USD")
                    .condition("Good")
                    .location("Los Angeles, CA")
                    .description("Some wear on box, game pieces in great shape")
                    .listedAt(LocalDateTime.now().minusDays(5))
                    .listingUrl("https://example.com/listing2")
                    .build(),
            GameListingDTO.builder()
                    .id("listing3")
                    .gameId(gameId)
                    .sellerName("BoardGameExchange")
                    .price(new BigDecimal("42.00"))
                    .currency("USD")
                    .condition("New")
                    .location("Chicago, IL")
                    .description("Brand new, sealed")
                    .listedAt(LocalDateTime.now().minusDays(1))
                    .listingUrl("https://example.com/listing3")
                    .build()
        );
    }
    
    private GameDetailsResponse mapGameToDetailsResponse(Game game) {
        return GameDetailsResponse.builder()
                .id(game.getId())
                .name(game.getName())
                .description(game.getDescription())
                .imageUrl(game.getImageUrl())
                .thumbnailUrl(game.getThumbnailUrl())
                .yearPublished(game.getYearPublished())
                .minPlayers(game.getMinPlayers())
                .maxPlayers(game.getMaxPlayers())
                .playingTime(game.getPlayingTime())
                .minAge(game.getMinAge())
                .categories(game.getCategories())
                .mechanics(game.getMechanics())
                .designers(game.getDesigners())
                .artists(game.getArtists())
                .publishers(game.getPublishers())
                .averageRating(game.getAverageRating())
                .ratingsCount(game.getRatingsCount())
                .listings(new ArrayList<>()) // Listings fetched separately
                .build();
    }
    
}
