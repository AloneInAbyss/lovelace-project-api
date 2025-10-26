package com.aloneinabyss.lovelace.pages.games.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "games")
public class Game {
    
    @Id
    private String id; // This will be the external API game ID
    
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
    
    @Indexed
    private LocalDateTime cachedAt;
    
    @Indexed
    private LocalDateTime expiresAt;
    
}
