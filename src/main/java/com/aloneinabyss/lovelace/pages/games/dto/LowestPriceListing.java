package com.aloneinabyss.lovelace.pages.games.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LowestPriceListing {
    private String listingId;
    private String condition;
    private BigDecimal price;
    private String city;
    private String state;
    private String listingUrl;
}
