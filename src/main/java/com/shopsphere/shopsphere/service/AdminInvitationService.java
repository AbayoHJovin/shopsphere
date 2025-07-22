package com.shopsphere.shopsphere.service;

import com.shopsphere.shopsphere.dto.request.AdminInvitationRequest;
import com.shopsphere.shopsphere.dto.request.InvitationAcceptRequest;
import com.shopsphere.shopsphere.dto.response.AdminInvitationResponse;
import com.shopsphere.shopsphere.dto.response.UserResponse;
import com.shopsphere.shopsphere.enums.Role;

import java.util.List;
import java.util.UUID;

public interface AdminInvitationService {

    AdminInvitationResponse createInvitation(AdminInvitationRequest request, String inviterEmail);

    UserResponse acceptInvitation(InvitationAcceptRequest request);

    void deleteInvitation(UUID invitationId);

    List<AdminInvitationResponse> getAllInvitations();

    AdminInvitationResponse getInvitationByToken(String token);

    boolean validateToken(String token);

    UserResponse promoteUser(UUID userId, Role newRole);

    UserResponse deleteUser(UUID userId);

    List<UserResponse> getUsersByRole(Role role);
}