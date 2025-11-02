package com.aloneinabyss.lovelace.pages.games.dto;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameSearchResponse {
    private String id;
    private String name;
    private Integer yearPublished;
    private Boolean isExpansion;
    private Map<String, LowestPriceListing> lowestPricesByCondition; // key: condition (new, used, auction)
}
