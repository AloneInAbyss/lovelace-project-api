package br.com.fiap.lovelace_project_api.exception;

public class WishlistItemAlreadyExistsException extends RuntimeException {
    
    public WishlistItemAlreadyExistsException(String message) {
        super(message);
    }
    
    public WishlistItemAlreadyExistsException(String gameId, String userId) {
        super(String.format("Game with ID '%s' is already in the wishlist for user '%s'", gameId, userId));
    }
    
}
