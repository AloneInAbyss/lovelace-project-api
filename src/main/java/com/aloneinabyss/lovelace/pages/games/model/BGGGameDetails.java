package com.aloneinabyss.lovelace.pages.games.model;

import com.opencsv.bean.CsvBindByName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "bgg_game_details")
public class BGGGameDetails {
    
    @Id
    private String mongoId;
    
    @CsvBindByName(column = "id")
    @Indexed(unique = true)
    private Long id;
    
    @CsvBindByName(column = "name")
    @Indexed
    private String name;
    
    @CsvBindByName(column = "yearpublished")
    private Integer yearpublished;
    
    @CsvBindByName(column = "rank")
    private Integer rank;
    
    @CsvBindByName(column = "bayesaverage")
    private Double bayesaverage;
    
    @CsvBindByName(column = "average")
    private Double average;
    
    @CsvBindByName(column = "usersrated")
    private Integer usersrated;
    
    @CsvBindByName(column = "is_expansion")
    private Integer isExpansion;
    
    @CsvBindByName(column = "abstracts_rank")
    private Integer abstractsRank;
    
    @CsvBindByName(column = "cgs_rank")
    private Integer cgsRank;
    
    @CsvBindByName(column = "childrensgames_rank")
    private Integer childrensgamesRank;
    
    @CsvBindByName(column = "familygames_rank")
    private Integer familygamesRank;
    
    @CsvBindByName(column = "partygames_rank")
    private Integer partygamesRank;
    
    @CsvBindByName(column = "strategygames_rank")
    private Integer strategygamesRank;
    
    @CsvBindByName(column = "thematic_rank")
    private Integer thematicRank;
    
    @CsvBindByName(column = "wargames_rank")
    private Integer wargamesRank;
    
}
