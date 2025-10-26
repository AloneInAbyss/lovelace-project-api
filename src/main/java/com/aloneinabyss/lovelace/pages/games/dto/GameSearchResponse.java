package com.aloneinabyss.lovelace.pages.games.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameSearchResponse {
    
    private List<GameSearchResultDTO> results;
    
    private int currentPage;
    
    private int totalPages;
    
    private long totalResults;
    
    private int pageSize;
    
}
