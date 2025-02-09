package pesco.withdrawal_service.controllers;

import java.math.BigDecimal;
import java.util.Arrays;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;
import pesco.withdrawal_service.exceptions.Error;
import pesco.withdrawal_service.payloads.WithdrawInRequest;
import pesco.withdrawal_service.payloads.WithdrawOutRequest;
import pesco.withdrawal_service.services.WithdrawalService;
import pesco.withdrawal_service.enums.CurrencyType;
import pesco.withdrawal_service.utils.TokenExtractor;

@RestController
@RequestMapping("/api/v1/withdraw")
public class WithdrawalController {

    private final WithdrawalService wallService;
    private final TokenExtractor tokenExtractor;
    private final HttpServletRequest httpServletRequest;

    public WithdrawalController(WithdrawalService wallService, TokenExtractor tokenExtractor, HttpServletRequest httpServletRequest) {
        this.wallService = wallService;
        this.tokenExtractor = tokenExtractor;
        this.httpServletRequest = httpServletRequest;
    }
  
    
    @PostMapping("/user/in/send")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> withUsernameToUserInSamePlatformUser(@RequestBody WithdrawInRequest request, Authentication authentication, HttpServletRequest httpRequest) {
        String username = authentication.getName();
        String token = tokenExtractor.extractToken(httpServletRequest);
        if (token.isBlank() || token.isEmpty()) {
            return Error.createResponse("UNAUTHORIZED*.",
                    HttpStatus.UNAUTHORIZED, "Require token to access this endpoint, Missing valid token.");
        }

        if (request.getSenderUser().isEmpty()) {
            return Error.createResponse("Sender id is require.", HttpStatus.BAD_REQUEST,
                    "Please provide the sender user id.");
        }

        if (request.getRecipientUser().isEmpty()) {
            return Error.createResponse("Recipient id is require.", HttpStatus.BAD_REQUEST,
                    "Please provide the Recipient user id.");
        }

        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return Error.createResponse("Amount is required and must be greater than zero.", HttpStatus.BAD_REQUEST,
                    "Please provide a valid amount you want to transfer to this user.");
        }

        String providedPin = request.getTransferpin();

        if (providedPin == null || providedPin.isEmpty()) {
            return Error.createResponse("Transfer pin is required.", HttpStatus.BAD_REQUEST,
                    "Please provide your transfer pin.");
        }

        if (!Arrays.stream(CurrencyType.values()).anyMatch(ct -> ct.name().equals(request.getCurrencyType().toString().toUpperCase()))) {
            return Error.createResponse("Invalid Currency provided.*.",
                    HttpStatus.BAD_REQUEST,
                    "Please provide Currency type. any of this list (USD, EUR, NGN,GBP, JPY,AUD,CAD, CHF, CNY, OR INR)");
        }
        
        return wallService.WithdrawUsingUsernameToUserInSamePlatform(username, request, authentication, token, httpRequest);
    }
    
    @SuppressWarnings("unlikely-arg-type")
    @PostMapping("/user/out/send")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> transferToExternalUserOutSidePlatform(@RequestBody WithdrawOutRequest request, Authentication authentication, HttpServletRequest httpRequest) {
        String username = authentication.getName();
        String token = tokenExtractor.extractToken(httpServletRequest); 
        if (token.isBlank() || token.isEmpty()) {
            return Error.createResponse("UNAUTHORIZED*.",
                    HttpStatus.UNAUTHORIZED, "Require token to access this endpoint, Missing valid token.");
        }

        if (request.getAmount() == null || request.getAmount().equals("")) {
            return Error.createResponse("Amount is require.*", HttpStatus.BAD_REQUEST,
                    "Please provide the amount you want to transfer to this user.");
        }

        String providedPin = request.getTransferpin();

        if (providedPin == null || providedPin.isEmpty()) {
            return Error.createResponse("Transfer pin is required.", HttpStatus.BAD_REQUEST,
                    "Please provide your transfer pin.");
        }

        if (!request.getCurrencyType().toString().isEmpty()
                || !request.getCurrencyType().toString().isBlank() && !Arrays.stream(CurrencyType.values())
                        .anyMatch(ct -> ct.name().equals(request.getCurrencyType().toString().toUpperCase()))) {
            return Error.createResponse("Invalid Currency provided.*.",
                    HttpStatus.BAD_REQUEST,
                    "Please provide Currency type. any of this list (USD, EUR, NGN,GBP, JPY,AUD,CAD, CHF, CNY, OR INR)");
        }
        return wallService.transferToExternalPlatform(username, request, authentication, token, httpRequest);
    }

}
