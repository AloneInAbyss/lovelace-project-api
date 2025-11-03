package com.aloneinabyss.lovelace.pages.wishlist.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aloneinabyss.lovelace.pages.games.dto.LowestPriceListing;
import com.aloneinabyss.lovelace.pages.games.model.GameDetails;
import com.aloneinabyss.lovelace.pages.games.model.GameListing;
import com.aloneinabyss.lovelace.pages.games.repository.GameDetailsRepository;
import com.aloneinabyss.lovelace.pages.games.repository.GameListingRepository;
import com.aloneinabyss.lovelace.pages.wishlist.dto.WishlistItemResponse;
import com.aloneinabyss.lovelace.pages.wishlist.model.WishlistItem;
import com.aloneinabyss.lovelace.pages.wishlist.repository.WishlistRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final GameDetailsRepository gameDetailsRepository;
    private final GameListingRepository gameListingRepository;

    public WishlistItemResponse addToWishlist(String userId, String gameId) {
        // Check if game exists
        GameDetails game = gameDetailsRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found with id: " + gameId));

        // Check if already in wishlist
        if (wishlistRepository.existsByUserIdAndGameId(userId, gameId)) {
            throw new RuntimeException("Game already in wishlist");
        }

        // Create wishlist item
        WishlistItem wishlistItem = WishlistItem.builder()
                .userId(userId)
                .gameId(gameId)
                .addedAt(LocalDateTime.now())
                .build();

        WishlistItem saved = wishlistRepository.save(wishlistItem);
        log.info("User {} added game {} to wishlist", userId, gameId);

        return mapToWishlistItemResponse(saved, game);
    }

    @Transactional
    public void removeFromWishlist(String userId, String gameId) {
        // Check if item exists in wishlist
        if (!wishlistRepository.existsByUserIdAndGameId(userId, gameId)) {
            throw new RuntimeException("Game not found in wishlist");
        }

        wishlistRepository.deleteByUserIdAndGameId(userId, gameId);
        log.info("User {} removed game {} from wishlist", userId, gameId);
    }

    public Page<WishlistItemResponse> getUserWishlist(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "addedAt"));
        
        Page<WishlistItem> wishlistPage = wishlistRepository.findByUserId(userId, pageable);
        
        return wishlistPage.map(wishlistItem -> {
            GameDetails game = gameDetailsRepository.findById(wishlistItem.getGameId())
                    .orElse(null);
            
            if (game == null) {
                log.warn("Game {} not found for wishlist item {}", wishlistItem.getGameId(), wishlistItem.getId());
                return null;
            }
            
            return mapToWishlistItemResponse(wishlistItem, game);
        });
    }

    private WishlistItemResponse mapToWishlistItemResponse(WishlistItem wishlistItem, GameDetails game) {
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

        return WishlistItemResponse.builder()
                .id(wishlistItem.getId())
                .gameId(game.getId())
                .gameName(game.getName())
                .yearPublished(game.getYearPublished())
                .isExpansion(game.getIsExpansion())
                .addedAt(wishlistItem.getAddedAt())
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
