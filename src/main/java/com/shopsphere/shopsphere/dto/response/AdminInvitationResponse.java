package com.shopsphere.shopsphere.dto.response;

import com.shopsphere.shopsphere.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminInvitationResponse {

    private UUID invitationId;
    private String email;
    private Role role;
    private LocalDateTime expiryDate;
    private LocalDateTime createdAt;
    private String inviterEmail;
    private String inviterUsername;
}