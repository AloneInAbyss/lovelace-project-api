package com.aloneinabyss.lovelace.pages.wishlist.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aloneinabyss.lovelace.pages.wishlist.dto.AddToWishlistRequest;
import com.aloneinabyss.lovelace.pages.wishlist.dto.WishlistItemResponse;
import com.aloneinabyss.lovelace.pages.wishlist.service.WishlistService;
import com.aloneinabyss.lovelace.security.UserPrincipal;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    /**
     * Add a game to the user's wishlist.
     * 
     * @param userPrincipal Authenticated user
     * @param request Request containing the game ID
     * @return Added wishlist item
     */
    @PostMapping
    public ResponseEntity<WishlistItemResponse> addToWishlist(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody AddToWishlistRequest request
    ) {
        log.info("User {} adding game {} to wishlist", userPrincipal.getId(), request.getGameId());
        
        WishlistItemResponse response = wishlistService.addToWishlist(
                userPrincipal.getId(), 
                request.getGameId()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Remove a game from the user's wishlist.
     * 
     * @param userPrincipal Authenticated user
     * @param gameId Game ID to remove
     * @return No content response
     */
    @DeleteMapping("/{gameId}")
    public ResponseEntity<Void> removeFromWishlist(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable String gameId
    ) {
        log.info("User {} removing game {} from wishlist", userPrincipal.getId(), gameId);
        
        wishlistService.removeFromWishlist(userPrincipal.getId(), gameId);
        
        return ResponseEntity.noContent().build();
    }

    /**
     * Get the user's wishlist with pagination.
     * 
     * @param userPrincipal Authenticated user
     * @param page Page number (0-indexed)
     * @param size Page size
     * @return Page of wishlist items
     */
    @GetMapping
    public ResponseEntity<Page<WishlistItemResponse>> getUserWishlist(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("User {} fetching wishlist, page: {}, size: {}", userPrincipal.getId(), page, size);
        
        Page<WishlistItemResponse> wishlist = wishlistService.getUserWishlist(
                userPrincipal.getId(), 
                page, 
                size
        );
        
        log.info("Found {} items in wishlist for user {}", wishlist.getTotalElements(), userPrincipal.getId());
        
        return ResponseEntity.ok(wishlist);
    }
}
