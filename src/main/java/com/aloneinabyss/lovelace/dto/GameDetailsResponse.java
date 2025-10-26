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
public class GameDetailsResponse {
    
    private String id;
    
    private String name;
    
    private String description;
    
    private String imageUrl;
    
    private String thumbnailUrl;
    
    private Integer yearPublished;
    
    private Integer minPlayers;
    
    private Integer maxPlayers;
    
    private Integer playingTime;
    
    private Integer minAge;
    
    private List<String> categories;
    
    private List<String> mechanics;
    
    private List<String> designers;
    
    private List<String> artists;
    
    private List<String> publishers;
    
    private Double averageRating;
    
    private Integer ratingsCount;
    
    private List<GameListingDTO> listings;
    
}
