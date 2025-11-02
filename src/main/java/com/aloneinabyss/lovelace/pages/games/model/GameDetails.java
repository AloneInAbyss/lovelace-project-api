package com.aloneinabyss.lovelace.pages.games.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.opencsv.bean.CsvBindByName;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "games")
public class GameDetails {

    @Id
    @CsvBindByName(column = "id")
    private String id;

    @Indexed(unique = true)
    @CsvBindByName(column = "name")
    private String name;

    @CsvBindByName(column = "yearpublished")
    private Integer yearPublished;

    @CsvBindByName(column = "is_expansion")
    private Boolean isExpansion;

}
