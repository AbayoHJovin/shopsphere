package com.shopsphere.shopsphere.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChangeEmailRequest {
    @NotBlank(message = "Password is required for verification")
    private String password;

    @NotBlank(message = "New email is required")
    @Email(message = "Email should be valid")
    private String newEmail;
}