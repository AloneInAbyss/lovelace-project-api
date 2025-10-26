package com.aloneinabyss.lovelace.pages.wishlist.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.aloneinabyss.lovelace.pages.wishlist.model.Wishlist;

import java.util.Optional;

@Repository
public interface WishlistRepository extends MongoRepository<Wishlist, String> {
    
    Optional<Wishlist> findByUserId(String userId);
    
    boolean existsByUserId(String userId);
    
    void deleteByUserId(String userId);
    
}
