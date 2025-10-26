package com.aloneinabyss.lovelace.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utility class to access the current authenticated user's information from JWT claims.
 * Provides convenient methods to retrieve userId, username, and roles without repository lookups.
 */
public class SecurityUtils {
    
    private SecurityUtils() {
        // Utility class - prevent instantiation
    }
    
    /**
     * Get the currently authenticated user's UserPrincipal.
     *
     * @return UserPrincipal if authenticated, null otherwise
     */
    public static UserPrincipal getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            return (UserPrincipal) authentication.getPrincipal();
        }
        return null;
    }
    
    /**
     * Get the currently authenticated user's ID from JWT claims.
     *
     * @return User ID if authenticated, null otherwise
     */
    public static String getCurrentUserId() {
        UserPrincipal user = getCurrentUser();
        return user != null ? user.getId() : null;
    }
    
    /**
     * Get the currently authenticated user's username from JWT claims.
     *
     * @return Username if authenticated, null otherwise
     */
    public static String getCurrentUsername() {
        UserPrincipal user = getCurrentUser();
        return user != null ? user.getUsername() : null;
    }
    
    /**
     * Check if the current user has a specific role.
     *
     * @param role The role to check (e.g., "ROLE_ADMIN")
     * @return true if user has the role, false otherwise
     */
    public static boolean hasRole(String role) {
        UserPrincipal user = getCurrentUser();
        if (user == null) {
            return false;
        }
        return user.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(role));
    }
    
    /**
     * Check if there is a currently authenticated user.
     *
     * @return true if user is authenticated, false otherwise
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && 
               authentication.isAuthenticated() && 
               authentication.getPrincipal() instanceof UserPrincipal;
    }
}
