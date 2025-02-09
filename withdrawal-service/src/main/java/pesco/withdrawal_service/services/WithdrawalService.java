package pesco.withdrawal_service.services;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import jakarta.servlet.http.HttpServletRequest;
import pesco.withdrawal_service.payloads.WithdrawInRequest;
import pesco.withdrawal_service.payloads.WithdrawOutRequest;

public interface WithdrawalService {

    ResponseEntity<?> transferToExternalPlatform(String username, WithdrawOutRequest request,
            Authentication authentication, String token, HttpServletRequest httpRequest);

    ResponseEntity<?> WithdrawUsingUsernameToUserInSamePlatform(String username, WithdrawInRequest request,
            Authentication authentication, String token, HttpServletRequest httpRequest);

}
