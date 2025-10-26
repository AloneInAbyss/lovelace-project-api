package com.aloneinabyss.lovelace.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {
    
    @GetMapping("/public")
    public ResponseEntity<String> publicEndpoint() {
        return ResponseEntity.ok("This is a public endpoint");
    }
    
    @GetMapping("/user")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, Object>> userEndpoint(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Hello, " + authentication.getName());
        response.put("authorities", authentication.getAuthorities());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> adminEndpoint() {
        return ResponseEntity.ok("This is an admin endpoint");
    }
    
}
