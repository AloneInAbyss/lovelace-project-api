package com.aloneinabyss.lovelace.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WishlistResponse {
    
    private List<WishlistItemResponse> items;
    
    private int currentPage;
    
    private int totalPages;
    
    private long totalItems;
    
    private int pageSize;
    
}
