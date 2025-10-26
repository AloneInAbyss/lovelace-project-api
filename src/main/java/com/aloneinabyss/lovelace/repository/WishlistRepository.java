package com.aloneinabyss.lovelace.repository;

import com.aloneinabyss.lovelace.model.Wishlist;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WishlistRepository extends MongoRepository<Wishlist, String> {
    
    Optional<Wishlist> findByUserId(String userId);
    
    boolean existsByUserId(String userId);
    
    void deleteByUserId(String userId);
    
}
