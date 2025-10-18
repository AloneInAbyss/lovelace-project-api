package br.com.fiap.lovelace_project_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameListingsResponse {
    
    private List<GameListingDTO> listings;
    
    private int currentPage;
    
    private int totalPages;
    
    private long totalListings;
    
    private int pageSize;
    
}
