package com.aloneinabyss.lovelace.pages.games.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "game_details")
public class GameDetails {
    
    @Id
    private String id;
    
    private String title;
    
    private String description;
    
    private String genre;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
}
