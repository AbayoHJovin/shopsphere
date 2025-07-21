package com.shopsphere.shopsphere.service;

public interface EmailService {
    void sendPasswordResetEmail(String to, String token);

    void sendWelcomeEmail(String to, String username);
}