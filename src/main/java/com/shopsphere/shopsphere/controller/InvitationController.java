package com.shopsphere.shopsphere.controller;

import com.shopsphere.shopsphere.dto.request.InvitationAcceptRequest;
import com.shopsphere.shopsphere.dto.response.ErrorResponse;
import com.shopsphere.shopsphere.dto.response.UserResponse;
import com.shopsphere.shopsphere.exception.ResourceNotFoundException;
import com.shopsphere.shopsphere.service.AdminInvitationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/invitations")
@RequiredArgsConstructor
@Slf4j
public class InvitationController {

    private final AdminInvitationService adminInvitationService;

    @GetMapping("/validate/{token}")
    public ResponseEntity<?> validateInvitation(
            @PathVariable String token,
            HttpServletRequest servletRequest) {
        try {
            log.info("Validating invitation token: {}", token);
            boolean isValid = adminInvitationService.validateToken(token);
            
            if (isValid) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.status(HttpStatus.GONE)
                        .body(ErrorResponse.of(HttpStatus.GONE,
                                "Invitation has expired or is invalid",
                                servletRequest.getRequestURI()));
            }
        } catch (Exception e) {
            log.error("Error validating invitation token", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error validating invitation token: " + e.getMessage(),
                            servletRequest.getRequestURI()));
        }
    }

    @PostMapping("/accept")
    public ResponseEntity<?> acceptInvitation(
            @Valid @RequestBody InvitationAcceptRequest request,
            HttpServletRequest servletRequest) {
        try {
            log.info("Accepting invitation with token: {}", request.getToken());
            UserResponse response = adminInvitationService.acceptInvitation(request);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            log.error("Invitation not found", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.of(HttpStatus.NOT_FOUND,
                            e.getMessage(),
                            servletRequest.getRequestURI()));
        } catch (IllegalStateException e) {
            log.error("Invalid invitation state", e);
            return ResponseEntity.status(HttpStatus.GONE)
                    .body(ErrorResponse.of(HttpStatus.GONE,
                            e.getMessage(),
                            servletRequest.getRequestURI()));
        } catch (Exception e) {
            log.error("Error accepting invitation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error accepting invitation: " + e.getMessage(),
                            servletRequest.getRequestURI()));
        }
    }
} 