package com.aloneinabyss.lovelace.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WishlistCheckResponse {
    
    private boolean inWishlist;
    
    private String wishlistItemId;
    
}
