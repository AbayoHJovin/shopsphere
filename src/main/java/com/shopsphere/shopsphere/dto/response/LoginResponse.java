package com.shopsphere.shopsphere.dto.response;

import com.shopsphere.shopsphere.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private UUID userId;
    private String username;
    private String email;
    private Role role;
    private String accessToken;
    private String refreshToken;
    private String tokenType;
}