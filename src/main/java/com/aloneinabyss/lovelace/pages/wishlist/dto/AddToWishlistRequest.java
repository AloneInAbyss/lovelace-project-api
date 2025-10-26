package com.aloneinabyss.lovelace.pages.wishlist.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddToWishlistRequest {
    
    @NotBlank(message = "Game ID is required")
    private String gameId;
    
    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;
    
}
