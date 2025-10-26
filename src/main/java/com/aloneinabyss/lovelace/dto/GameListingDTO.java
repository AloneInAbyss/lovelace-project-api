package com.aloneinabyss.lovelace.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameListingDTO {
    
    private String id;
    
    private String gameId;
    
    private String sellerName;
    
    private BigDecimal price;
    
    private String currency;
    
    private String condition;
    
    private String location;
    
    private String description;
    
    private LocalDateTime listedAt;
    
    private String listingUrl;
    
}
