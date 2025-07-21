package com.shopsphere.shopsphere.service;

import com.shopsphere.shopsphere.dto.request.*;
import com.shopsphere.shopsphere.dto.response.*;

public interface AuthService {
    SignupResponse signup(SignupRequest request);

    LoginResponse login(LoginRequest request);

    RefreshTokenResponse refreshToken(RefreshTokenRequest request);

    MessageResponse logout(LogoutRequest request);

    MessageResponse forgotPassword(ForgotPasswordRequest request);

    MessageResponse resetPassword(ResetPasswordRequest request);

    UserResponse updateAccount(UpdateAccountRequest request);

    UserResponse getCurrentUser();

    MessageResponse changePassword(ChangePasswordRequest request);

    MessageResponse changeEmail(ChangeEmailRequest request);
}