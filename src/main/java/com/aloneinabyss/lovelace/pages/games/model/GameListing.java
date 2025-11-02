package com.aloneinabyss.lovelace.pages.games.model;

import java.math.BigDecimal;

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
@Document(collection = "game_listings")
public class GameListing {

    @Id
    @CsvBindByName(column = "listing_id")
    private String listingId;

    @Indexed
    @CsvBindByName(column = "game_id")
    private String gameId;

    @CsvBindByName(column = "condition")
    private String condition; // "new", "used", or "auction"

    @CsvBindByName(column = "price")
    private BigDecimal price;

    @CsvBindByName(column = "city")
    private String city;

    @CsvBindByName(column = "state")
    private String state;

    @CsvBindByName(column = "listing_url")
    private String listingUrl;
}
