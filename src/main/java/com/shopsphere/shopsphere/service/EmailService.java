package com.shopsphere.shopsphere.service;

import java.util.Map;

public interface EmailService {

    void sendSimpleEmail(String to, String subject, String text);

    void sendHtmlEmail(String to, String subject, String templateName, Map<String, Object> templateModel);
    
    void sendWelcomeEmail(String to, String username);
    
    void sendPasswordResetEmail(String to, String resetToken);
}