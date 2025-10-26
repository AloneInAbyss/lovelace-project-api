package com.aloneinabyss.lovelace.pages.wishlist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WishlistItemResponse {
    
    private String gameId;
    
    private String gameName;
    
    private String gameImageUrl;
    
    private String gameThumbnailUrl;
    
    private Integer yearPublished;
    
    private Integer minPlayers;
    
    private Integer maxPlayers;
    
    private Integer playingTime;
    
    private Double averageRating;
    
    private String notes;
    
    private LocalDateTime addedAt;
    
    private LocalDateTime updatedAt;
    
}
