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
public class GameSearchResultDTO {
    
    private String id;
    
    private String name;
    
    private String thumbnailUrl;
    
    private Integer yearPublished;
    
    private Integer minPlayers;
    
    private Integer maxPlayers;
    
    private Integer playingTime;
    
    private Double averageRating;
    
    private Integer ratingsCount;
    
    private List<String> categories;
    
}
