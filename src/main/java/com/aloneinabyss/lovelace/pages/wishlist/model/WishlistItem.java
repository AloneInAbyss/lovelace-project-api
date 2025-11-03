package com.aloneinabyss.lovelace.pages.wishlist.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "wishlists")
@CompoundIndex(name = "user_game_idx", def = "{'userId': 1, 'gameId': 1}", unique = true)
public class WishlistItem {

    @Id
    private String id;

    private String userId;

    private String gameId;

    private LocalDateTime addedAt;
}
