package com.aloneinabyss.lovelace.pages.wishlist.service;

import com.aloneinabyss.lovelace.pages.games.model.Game;
import com.aloneinabyss.lovelace.pages.games.repository.GameRepository;
import com.aloneinabyss.lovelace.pages.wishlist.dto.AddToWishlistRequest;
import com.aloneinabyss.lovelace.pages.wishlist.dto.UpdateWishlistRequest;
import com.aloneinabyss.lovelace.pages.wishlist.dto.WishlistCheckResponse;
import com.aloneinabyss.lovelace.pages.wishlist.dto.WishlistItemResponse;
import com.aloneinabyss.lovelace.pages.wishlist.dto.WishlistResponse;
import com.aloneinabyss.lovelace.pages.wishlist.exception.WishlistItemAlreadyExistsException;
import com.aloneinabyss.lovelace.pages.wishlist.exception.WishlistItemNotFoundException;
import com.aloneinabyss.lovelace.pages.wishlist.model.Wishlist;
import com.aloneinabyss.lovelace.pages.wishlist.repository.WishlistRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WishlistService {
    
    private final WishlistRepository wishlistRepository;
    private final GameRepository gameRepository;
    
    @Transactional
    @CacheEvict(value = "wishlist", key = "#userId")
    public WishlistItemResponse addToWishlist(String userId, AddToWishlistRequest request) {
        log.info("Adding game {} to wishlist for user {}", request.getGameId(), userId);
        
        // Get or create wishlist for user
        Wishlist wishlist = wishlistRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Wishlist newWishlist = Wishlist.builder()
                            .userId(userId)
                            .items(new ArrayList<>())
                            .createdAt(LocalDateTime.now())
                            .build();
                    return wishlistRepository.save(newWishlist);
                });
        
        // Check if game already exists in wishlist
        boolean gameExists = wishlist.getItems().stream()
                .anyMatch(item -> item.getGameId().equals(request.getGameId()));
        
        if (gameExists) {
            throw new WishlistItemAlreadyExistsException(request.getGameId(), userId);
        }
        
        // Add new item to wishlist
        Wishlist.WishlistItem newItem = Wishlist.WishlistItem.builder()
                .gameId(request.getGameId())
                .notes(request.getNotes())
                .addedAt(LocalDateTime.now())
                .build();
        
        wishlist.getItems().add(newItem);
        wishlist.setUpdatedAt(LocalDateTime.now());
        
        wishlistRepository.save(wishlist);
        
        log.info("Game {} successfully added to wishlist for user {}", request.getGameId(), userId);
        
        // Fetch game details and return response
        return buildWishlistItemResponse(newItem);
    }
    
    @Transactional
    @CacheEvict(value = "wishlist", key = "#userId")
    public void removeFromWishlist(String userId, String gameId) {
        log.info("Removing game {} from wishlist for user {}", gameId, userId);
        
        Wishlist wishlist = wishlistRepository.findByUserId(userId)
                .orElseThrow(() -> new WishlistItemNotFoundException(gameId, userId));
        
        boolean removed = wishlist.getItems().removeIf(item -> item.getGameId().equals(gameId));
        
        if (!removed) {
            throw new WishlistItemNotFoundException(gameId, userId);
        }
        
        wishlist.setUpdatedAt(LocalDateTime.now());
        wishlistRepository.save(wishlist);
        
        log.info("Game {} successfully removed from wishlist for user {}", gameId, userId);
    }
    
    @Cacheable(value = "wishlist", key = "#userId + '-' + #page + '-' + #size + '-' + #sortBy")
    public WishlistResponse getUserWishlist(String userId, int page, int size, String sortBy) {
        log.info("Fetching wishlist for user {} - page: {}, size: {}, sortBy: {}", userId, page, size, sortBy);
        
        Wishlist wishlist = wishlistRepository.findByUserId(userId)
                .orElse(Wishlist.builder()
                        .userId(userId)
                        .items(new ArrayList<>())
                        .build());
        
        List<Wishlist.WishlistItem> items = wishlist.getItems();
        
        // Sort items
        List<Wishlist.WishlistItem> sortedItems = sortItems(items, sortBy);
        
        // Calculate pagination
        int totalItems = sortedItems.size();
        int totalPages = (int) Math.ceil((double) totalItems / size);
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, totalItems);
        
        // Get paginated items
        List<Wishlist.WishlistItem> paginatedItems = fromIndex < totalItems 
                ? sortedItems.subList(fromIndex, toIndex) 
                : new ArrayList<>();
        
        // Fetch game details for all items in this page
        List<String> gameIds = paginatedItems.stream()
                .map(Wishlist.WishlistItem::getGameId)
                .collect(Collectors.toList());
        
        Map<String, Game> gamesMap = gameRepository.findByIdIn(gameIds).stream()
                .collect(Collectors.toMap(Game::getId, game -> game));
        
        // Build response items
        List<WishlistItemResponse> responseItems = paginatedItems.stream()
                .map(item -> buildWishlistItemResponse(item, gamesMap.get(item.getGameId())))
                .collect(Collectors.toList());
        
        return WishlistResponse.builder()
                .items(responseItems)
                .currentPage(page)
                .totalPages(totalPages)
                .totalItems(totalItems)
                .pageSize(size)
                .build();
    }
    
    @Transactional
    @CacheEvict(value = "wishlist", key = "#userId")
    public WishlistItemResponse updateWishlistItem(String userId, String gameId, UpdateWishlistRequest request) {
        log.info("Updating wishlist item {} for user {}", gameId, userId);
        
        Wishlist wishlist = wishlistRepository.findByUserId(userId)
                .orElseThrow(() -> new WishlistItemNotFoundException(gameId, userId));
        
        Wishlist.WishlistItem item = wishlist.getItems().stream()
                .filter(i -> i.getGameId().equals(gameId))
                .findFirst()
                .orElseThrow(() -> new WishlistItemNotFoundException(gameId, userId));
        
        item.setNotes(request.getNotes());
        item.setUpdatedAt(LocalDateTime.now());
        wishlist.setUpdatedAt(LocalDateTime.now());
        
        wishlistRepository.save(wishlist);
        
        log.info("Wishlist item {} successfully updated for user {}", gameId, userId);
        
        return buildWishlistItemResponse(item);
    }
    
    public WishlistCheckResponse isGameInWishlist(String userId, String gameId) {
        log.debug("Checking if game {} is in wishlist for user {}", gameId, userId);
        
        return wishlistRepository.findByUserId(userId)
                .map(wishlist -> {
                    boolean inWishlist = wishlist.getItems().stream()
                            .anyMatch(item -> item.getGameId().equals(gameId));
                    return WishlistCheckResponse.builder()
                            .inWishlist(inWishlist)
                            .wishlistItemId(inWishlist ? wishlist.getId() : null)
                            .build();
                })
                .orElse(WishlistCheckResponse.builder()
                        .inWishlist(false)
                        .build());
    }
    
    @Transactional
    @CacheEvict(value = "wishlist", key = "#userId")
    public void clearWishlist(String userId) {
        log.info("Clearing wishlist for user {}", userId);
        
        wishlistRepository.findByUserId(userId).ifPresent(wishlist -> {
            wishlist.getItems().clear();
            wishlist.setUpdatedAt(LocalDateTime.now());
            wishlistRepository.save(wishlist);
        });
        
        log.info("Wishlist cleared for user {}", userId);
    }
    
    public long getWishlistCount(String userId) {
        return wishlistRepository.findByUserId(userId)
                .map(wishlist -> (long) wishlist.getItems().size())
                .orElse(0L);
    }
    
    private List<Wishlist.WishlistItem> sortItems(List<Wishlist.WishlistItem> items, String sortBy) {
        Comparator<Wishlist.WishlistItem> comparator;
        
        switch (sortBy.toLowerCase()) {
            case "addedat":
            case "added_at":
                comparator = Comparator.comparing(Wishlist.WishlistItem::getAddedAt).reversed();
                break;
            case "updatedat":
            case "updated_at":
                comparator = Comparator.comparing(item -> 
                    item.getUpdatedAt() != null ? item.getUpdatedAt() : item.getAddedAt(),
                    Comparator.nullsLast(Comparator.reverseOrder()));
                break;
            default:
                comparator = Comparator.comparing(Wishlist.WishlistItem::getAddedAt).reversed();
        }
        
        return items.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }
    
    private WishlistItemResponse buildWishlistItemResponse(Wishlist.WishlistItem item) {
        Game game = gameRepository.findById(item.getGameId()).orElse(null);
        return buildWishlistItemResponse(item, game);
    }
    
    private WishlistItemResponse buildWishlistItemResponse(Wishlist.WishlistItem item, Game game) {
        WishlistItemResponse.WishlistItemResponseBuilder builder = WishlistItemResponse.builder()
                .gameId(item.getGameId())
                .notes(item.getNotes())
                .addedAt(item.getAddedAt())
                .updatedAt(item.getUpdatedAt());
        
        if (game != null) {
            builder.gameName(game.getName())
                    .gameImageUrl(game.getImageUrl())
                    .gameThumbnailUrl(game.getThumbnailUrl())
                    .yearPublished(game.getYearPublished())
                    .minPlayers(game.getMinPlayers())
                    .maxPlayers(game.getMaxPlayers())
                    .playingTime(game.getPlayingTime())
                    .averageRating(game.getAverageRating());
        }
        
        return builder.build();
    }
    
}
