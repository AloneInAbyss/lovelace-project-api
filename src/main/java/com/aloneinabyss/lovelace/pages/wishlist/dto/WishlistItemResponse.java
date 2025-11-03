package com.aloneinabyss.lovelace.pages.wishlist.dto;

import java.time.LocalDateTime;
import java.util.Map;

import com.aloneinabyss.lovelace.pages.games.dto.LowestPriceListing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WishlistItemResponse {
    private String id;
    private String gameId;
    private String gameName;
    private Integer yearPublished;
    private Boolean isExpansion;
    private LocalDateTime addedAt;
    private Map<String, LowestPriceListing> lowestPricesByCondition;
}
