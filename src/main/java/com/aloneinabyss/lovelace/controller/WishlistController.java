package com.aloneinabyss.lovelace.controller;

import com.aloneinabyss.lovelace.dto.AddToWishlistRequest;
import com.aloneinabyss.lovelace.dto.MessageResponse;
import com.aloneinabyss.lovelace.dto.UpdateWishlistRequest;
import com.aloneinabyss.lovelace.dto.WishlistCheckResponse;
import com.aloneinabyss.lovelace.dto.WishlistItemResponse;
import com.aloneinabyss.lovelace.dto.WishlistResponse;
import com.aloneinabyss.lovelace.service.WishlistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistController {
    
    private final WishlistService wishlistService;
    
    @GetMapping
    public ResponseEntity<WishlistResponse> getWishlist(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "addedAt") String sortBy
    ) {
        String userId = authentication.getName();
        WishlistResponse response = wishlistService.getUserWishlist(userId, page, size, sortBy);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping
    public ResponseEntity<WishlistItemResponse> addToWishlist(
            Authentication authentication,
            @Valid @RequestBody AddToWishlistRequest request
    ) {
        String userId = authentication.getName();
        WishlistItemResponse response = wishlistService.addToWishlist(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @DeleteMapping("/{gameId}")
    public ResponseEntity<MessageResponse> removeFromWishlist(
            Authentication authentication,
            @PathVariable String gameId
    ) {
        String userId = authentication.getName();
        wishlistService.removeFromWishlist(userId, gameId);
        return ResponseEntity.ok(MessageResponse.builder()
                .message("Game removed from wishlist successfully")
                .build());
    }
    
    @PutMapping("/{gameId}")
    public ResponseEntity<WishlistItemResponse> updateWishlistItem(
            Authentication authentication,
            @PathVariable String gameId,
            @Valid @RequestBody UpdateWishlistRequest request
    ) {
        String userId = authentication.getName();
        WishlistItemResponse response = wishlistService.updateWishlistItem(userId, gameId, request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/check/{gameId}")
    public ResponseEntity<WishlistCheckResponse> checkGameInWishlist(
            Authentication authentication,
            @PathVariable String gameId
    ) {
        String userId = authentication.getName();
        WishlistCheckResponse response = wishlistService.isGameInWishlist(userId, gameId);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/clear")
    public ResponseEntity<MessageResponse> clearWishlist(Authentication authentication) {
        String userId = authentication.getName();
        wishlistService.clearWishlist(userId);
        return ResponseEntity.ok(MessageResponse.builder()
                .message("Wishlist cleared successfully")
                .build());
    }
    
    @GetMapping("/count")
    public ResponseEntity<Long> getWishlistCount(Authentication authentication) {
        String userId = authentication.getName();
        long count = wishlistService.getWishlistCount(userId);
        return ResponseEntity.ok(count);
    }
    
}
