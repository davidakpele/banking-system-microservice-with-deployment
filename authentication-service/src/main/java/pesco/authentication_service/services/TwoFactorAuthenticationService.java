package pesco.authentication_service.services;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import pesco.authentication_service.models.TwoFactorAuthentication;
import pesco.authentication_service.models.Users;
import pesco.authentication_service.payloads.OTPRequest;

public interface TwoFactorAuthenticationService {

    TwoFactorAuthentication createTwoFactorOtp(Users authUser, String otp, String jwtToken);

    TwoFactorAuthentication findByUser(Long userid);

    TwoFactorAuthentication findById(Long id);

    boolean verifyTwoFactorOtp(TwoFactorAuthentication twoFactorOTP, String otp);

    void deleteTwoFactorOtp(TwoFactorAuthentication twoFactorOTP);

    ResponseEntity<?> findByToken(String token);

    ResponseEntity<?> verifyUserTwoFactorOtp(OTPRequest reqOtpPayload);

    ResponseEntity<?> enableTwoFactorKey(Boolean enable2fa, Authentication authentication);
}