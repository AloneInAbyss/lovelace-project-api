package com.aloneinabyss.lovelace.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "wishlists")
public class Wishlist {
    
    @Id
    private String id;
    
    @Indexed(unique = true)
    private String userId;
    
    @Builder.Default
    private List<WishlistItem> items = new ArrayList<>();
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WishlistItem {
        
        private String gameId;
        
        private String notes;
        
        @Builder.Default
        private LocalDateTime addedAt = LocalDateTime.now();
        
        private LocalDateTime updatedAt;
        
    }
    
}
