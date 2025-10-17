package br.com.fiap.lovelace_project_api.exception;

public class WishlistItemNotFoundException extends RuntimeException {
    
    public WishlistItemNotFoundException(String message) {
        super(message);
    }
    
    public WishlistItemNotFoundException(String gameId, String userId) {
        super(String.format("Game with ID '%s' not found in wishlist for user '%s'", gameId, userId));
    }
    
}
