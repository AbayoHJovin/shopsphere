package com.shopsphere.shopsphere.controller;

import com.shopsphere.shopsphere.dto.request.AdminInvitationRequest;
import com.shopsphere.shopsphere.dto.request.InvitationAcceptRequest;
import com.shopsphere.shopsphere.dto.response.AdminInvitationResponse;
import com.shopsphere.shopsphere.dto.response.ErrorResponse;
import com.shopsphere.shopsphere.dto.response.MessageResponse;
import com.shopsphere.shopsphere.dto.response.UserResponse;
import com.shopsphere.shopsphere.enums.Role;
import com.shopsphere.shopsphere.exception.ResourceNotFoundException;
import com.shopsphere.shopsphere.service.AdminInvitationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/management")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminManagementController {

    private final AdminInvitationService adminInvitationService;

    @PostMapping("/invite")
    public ResponseEntity<?> inviteUser(
            @Valid @RequestBody AdminInvitationRequest request,
            Authentication authentication,
            HttpServletRequest servletRequest) {
        try {
            log.info("Inviting user with email: {} as {}", request.getEmail(), request.getRole());
            String inviterEmail = authentication.getName();
            AdminInvitationResponse response = adminInvitationService.createInvitation(request, inviterEmail);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error inviting user", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error inviting user: " + e.getMessage(),
                            servletRequest.getRequestURI()));
        }
    }

    @GetMapping("/invitations")
    public ResponseEntity<?> getAllInvitations(HttpServletRequest servletRequest) {
        try {
            log.info("Fetching all invitations");
            List<AdminInvitationResponse> invitations = adminInvitationService.getAllInvitations();
            return ResponseEntity.ok(invitations);
        } catch (Exception e) {
            log.error("Error fetching invitations", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error fetching invitations: " + e.getMessage(),
                            servletRequest.getRequestURI()));
        }
    }

    @DeleteMapping("/invitations/{invitationId}")
    public ResponseEntity<?> deleteInvitation(
            @PathVariable UUID invitationId,
            HttpServletRequest servletRequest) {
        try {
            log.info("Deleting invitation with ID: {}", invitationId);
            adminInvitationService.deleteInvitation(invitationId);
            return ResponseEntity.ok(MessageResponse.builder()
                    .message("Invitation deleted successfully")
                    .success(true)
                    .build());
        } catch (ResourceNotFoundException e) {
            log.error("Invitation not found", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.of(HttpStatus.NOT_FOUND,
                            e.getMessage(),
                            servletRequest.getRequestURI()));
        } catch (Exception e) {
            log.error("Error deleting invitation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error deleting invitation: " + e.getMessage(),
                            servletRequest.getRequestURI()));
        }
    }

    @GetMapping("/users/admins")
    public ResponseEntity<?> getAllAdmins(HttpServletRequest servletRequest) {
        try {
            log.info("Fetching all admins");
            List<UserResponse> admins = adminInvitationService.getUsersByRole(Role.ADMIN);
            return ResponseEntity.ok(admins);
        } catch (Exception e) {
            log.error("Error fetching admins", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error fetching admins: " + e.getMessage(),
                            servletRequest.getRequestURI()));
        }
    }

    @GetMapping("/users/co-workers")
    public ResponseEntity<?> getAllCoWorkers(HttpServletRequest servletRequest) {
        try {
            log.info("Fetching all co-workers");
            List<UserResponse> coWorkers = adminInvitationService.getUsersByRole(Role.CO_WORKER);
            return ResponseEntity.ok(coWorkers);
        } catch (Exception e) {
            log.error("Error fetching co-workers", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error fetching co-workers: " + e.getMessage(),
                            servletRequest.getRequestURI()));
        }
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUser(
            @PathVariable UUID userId,
            HttpServletRequest servletRequest) {
        try {
            log.info("Deleting user with ID: {}", userId);
            UserResponse deletedUser = adminInvitationService.deleteUser(userId);
            return ResponseEntity.ok(MessageResponse.builder()
                    .message("User deleted successfully")
                    .success(true)
                    .build());
        } catch (IllegalStateException e) {
            log.error("Cannot delete protected admin", e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.of(HttpStatus.FORBIDDEN,
                            e.getMessage(),
                            servletRequest.getRequestURI()));
        } catch (ResourceNotFoundException e) {
            log.error("User not found", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.of(HttpStatus.NOT_FOUND,
                            e.getMessage(),
                            servletRequest.getRequestURI()));
        } catch (Exception e) {
            log.error("Error deleting user", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error deleting user: " + e.getMessage(),
                            servletRequest.getRequestURI()));
        }
    }

    @PatchMapping("/users/{userId}/promote")
    public ResponseEntity<?> promoteUser(
            @PathVariable UUID userId,
            @RequestParam Role role,
            HttpServletRequest servletRequest) {
        try {
            log.info("Promoting user with ID: {} to role: {}", userId, role);
            UserResponse updatedUser = adminInvitationService.promoteUser(userId, role);
            return ResponseEntity.ok(updatedUser);
        } catch (IllegalStateException e) {
            log.error("Cannot change role of protected admin", e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.of(HttpStatus.FORBIDDEN,
                            e.getMessage(),
                            servletRequest.getRequestURI()));
        } catch (ResourceNotFoundException e) {
            log.error("User not found", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.of(HttpStatus.NOT_FOUND,
                            e.getMessage(),
                            servletRequest.getRequestURI()));
        } catch (Exception e) {
            log.error("Error promoting user", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error promoting user: " + e.getMessage(),
                            servletRequest.getRequestURI()));
        }
    }
}