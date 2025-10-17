package br.com.fiap.lovelace_project_api.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateWishlistRequest {
    
    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;
    
}
