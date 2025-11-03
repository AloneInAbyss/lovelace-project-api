package com.aloneinabyss.lovelace.pages.wishlist.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.aloneinabyss.lovelace.pages.wishlist.model.WishlistItem;

import java.util.Optional;

@Repository
public interface WishlistRepository extends MongoRepository<WishlistItem, String> {
    
    Page<WishlistItem> findByUserId(String userId, Pageable pageable);
    
    Optional<WishlistItem> findByUserIdAndGameId(String userId, String gameId);
    
    boolean existsByUserIdAndGameId(String userId, String gameId);
    
    void deleteByUserIdAndGameId(String userId, String gameId);
}
