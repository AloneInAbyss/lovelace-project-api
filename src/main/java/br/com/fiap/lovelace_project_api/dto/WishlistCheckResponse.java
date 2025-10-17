package br.com.fiap.lovelace_project_api.dto;

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
