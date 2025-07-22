package com.shopsphere.shopsphere.service.impl;

import com.shopsphere.shopsphere.dto.request.AdminInvitationRequest;
import com.shopsphere.shopsphere.dto.request.InvitationAcceptRequest;
import com.shopsphere.shopsphere.dto.response.AdminInvitationResponse;
import com.shopsphere.shopsphere.dto.response.UserResponse;
import com.shopsphere.shopsphere.enums.Role;
import com.shopsphere.shopsphere.exception.ResourceNotFoundException;
import com.shopsphere.shopsphere.models.AdminInvitation;
import com.shopsphere.shopsphere.models.User;
import com.shopsphere.shopsphere.repository.AdminInvitationRepository;
import com.shopsphere.shopsphere.repository.UserRepository;
import com.shopsphere.shopsphere.service.AdminInvitationService;
import com.shopsphere.shopsphere.service.EmailService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminInvitationServiceImpl implements AdminInvitationService {

    private final AdminInvitationRepository adminInvitationRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${app.invitation.expiration-minutes:1440}") // 24 hours by default
    private long invitationExpirationMinutes;

    @Value("${app.admin.protected-email:abayohirwajovin@gmail.com}")
    private String protectedAdminEmail;

    @Override
    @Transactional
    public AdminInvitationResponse createInvitation(AdminInvitationRequest request, String inviterEmail) {
        log.info("Creating invitation for email: {} with role: {}", request.getEmail(), request.getRole());

        // Check if user with this email already exists
        boolean userExists = userRepository.existsByEmail(request.getEmail());

        // Get inviter
        User inviter = userRepository.findByEmail(inviterEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Inviter not found"));

        // Delete any existing invitations for this email
        if (adminInvitationRepository.existsByEmail(request.getEmail())) {
            adminInvitationRepository.deleteByEmail(request.getEmail());
        }

        // Create new invitation
        String token = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(invitationExpirationMinutes);

        AdminInvitation invitation = AdminInvitation.builder()
                .email(request.getEmail())
                .token(token)
                .expiryDate(expiryDate)
                .role(request.getRole())
                .inviter(inviter)
                .build();

        AdminInvitation savedInvitation = adminInvitationRepository.save(invitation);

        // Send email
        sendInvitationEmail(savedInvitation, userExists);

        return mapToResponse(savedInvitation);
    }

    @Override
    @Transactional
    public UserResponse acceptInvitation(InvitationAcceptRequest request) {
        log.info("Accepting invitation with token: {}", request.getToken());

        AdminInvitation invitation = adminInvitationRepository.findByToken(request.getToken())
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found or expired"));

        if (invitation.isExpired()) {
            adminInvitationRepository.delete(invitation);
            throw new IllegalStateException("Invitation has expired");
        }

        // Check if user already exists
        User user;
        boolean isNewUser = !userRepository.existsByEmail(invitation.getEmail());

        if (isNewUser) {
            // Create new user
            user = User.builder()
                    .email(invitation.getEmail())
                    .username(request.getUsername() != null ? request.getUsername()
                            : generateUsernameFromEmail(invitation.getEmail()))
                    .password(passwordEncoder.encode(request.getPassword()))
                    .role(invitation.getRole())
                    .active(true)
                    .build();
        } else {
            // Update existing user's role
            user = userRepository.findByEmail(invitation.getEmail())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            user.setRole(invitation.getRole());
        }

        User savedUser = userRepository.save(user);

        // Delete the invitation
        adminInvitationRepository.delete(invitation);

        return mapUserToResponse(savedUser);
    }

    @Override
    @Transactional
    public void deleteInvitation(UUID invitationId) {
        log.info("Deleting invitation with ID: {}", invitationId);

        AdminInvitation invitation = adminInvitationRepository.findById(invitationId)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found"));

        adminInvitationRepository.delete(invitation);
    }

    @Override
    public List<AdminInvitationResponse> getAllInvitations() {
        log.info("Fetching all invitations");

        return adminInvitationRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public AdminInvitationResponse getInvitationByToken(String token) {
        log.info("Fetching invitation by token: {}", token);

        AdminInvitation invitation = adminInvitationRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found"));

        return mapToResponse(invitation);
    }

    @Override
    public boolean validateToken(String token) {
        log.info("Validating token: {}", token);

        return adminInvitationRepository.findByToken(token)
                .map(invitation -> !invitation.isExpired())
                .orElse(false);
    }

    @Override
    @Transactional
    public UserResponse promoteUser(UUID userId, Role newRole) {
        log.info("Promoting user with ID: {} to role: {}", userId, newRole);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check if this is the protected admin
        if (user.getEmail().equals(protectedAdminEmail) && user.getRole() == Role.ADMIN && newRole != Role.ADMIN) {
            throw new IllegalStateException("Cannot change role of the protected admin");
        }

        user.setRole(newRole);
        User updatedUser = userRepository.save(user);

        return mapUserToResponse(updatedUser);
    }

    @Override
    @Transactional
    public UserResponse deleteUser(UUID userId) {
        log.info("Deleting user with ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check if this is the protected admin
        if (user.getEmail().equals(protectedAdminEmail)) {
            throw new IllegalStateException("Cannot delete the protected admin");
        }

        // Store user data for response before deletion
        UserResponse response = mapUserToResponse(user);

        userRepository.delete(user);

        return response;
    }

    @Override
    public List<UserResponse> getUsersByRole(Role role) {
        log.info("Fetching users with role: {}", role);

        return userRepository.findByRole(role).stream()
                .map(this::mapUserToResponse)
                .collect(Collectors.toList());
    }

    // Helper methods
    private void sendInvitationEmail(AdminInvitation invitation, boolean userExists) {
        String subject = "Invitation to join ShopSphere as " +
                (invitation.getRole() == Role.ADMIN ? "Admin" : "Co-worker");

        String invitationLink;
        if (userExists) {
            // If user exists, send link to login page
            invitationLink = frontendUrl + "/login?invitation=" + invitation.getToken();
        } else {
            // If new user, send link to password setup page
            invitationLink = frontendUrl + "/setup-password?token=" + invitation.getToken();
        }

        Map<String, Object> templateModel = new HashMap<>();
        templateModel.put("invitationLink", invitationLink);
        templateModel.put("role", invitation.getRole().toString());
        templateModel.put("inviterName", invitation.getInviter().getUsername());
        templateModel.put("expiryDate", invitation.getExpiryDate());

        String template = userExists ? "invitation-existing-user" : "invitation-new-user";

        emailService.sendHtmlEmail(invitation.getEmail(), subject, template, templateModel);
    }

    private String generateUsernameFromEmail(String email) {
        return email.split("@")[0];
    }

    private AdminInvitationResponse mapToResponse(AdminInvitation invitation) {
        return AdminInvitationResponse.builder()
                .invitationId(invitation.getInvitationId())
                .email(invitation.getEmail())
                .role(invitation.getRole())
                .expiryDate(invitation.getExpiryDate())
                .createdAt(invitation.getCreatedAt())
                .inviterEmail(invitation.getInviter().getEmail())
                .inviterUsername(invitation.getInviter().getUsername())
                .build();
    }

    private UserResponse mapUserToResponse(User user) {
        return UserResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .profilePicture(user.getProfilePicture())
                .createdAt(user.getCreatedAt())
                .active(user.isActive())
                .build();
    }
}