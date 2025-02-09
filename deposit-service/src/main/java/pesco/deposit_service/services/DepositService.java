package pesco.deposit_service.services;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import pesco.deposit_service.payloads.DepositRequest;

public interface DepositService {
    ResponseEntity<?> createDeposit(DepositRequest request, String token, Authentication authentication);
}
