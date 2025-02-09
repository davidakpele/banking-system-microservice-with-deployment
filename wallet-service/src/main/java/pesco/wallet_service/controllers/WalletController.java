package pesco.wallet_service.controllers;

import java.util.Arrays;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import pesco.wallet_service.enums.CurrencyType;
import pesco.wallet_service.services.WalletService;
import pesco.wallet_service.utils.TokenExtractor;
import pesco.wallet_service.exceptions.Error;
import pesco.wallet_service.payloads.CreateWalletRequest;
import pesco.wallet_service.payloads.DeductAmountRequest;
import pesco.wallet_service.payloads.TransactionRequest;
import pesco.wallet_service.payloads.UpdateWalletRequest;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/wallet")
public class WalletController {

    private final WalletService walletService;
    private final TokenExtractor tokenExtractor;
    private final HttpServletRequest httpServletRequest;

    @PostMapping("/create")
    public ResponseEntity<?> createWallet(@RequestBody CreateWalletRequest request) {
        try {
            // Call the service layer to create the wallet
            if (request.getUserId() ==null || request.getUserId() <=0) {
                return Error.createResponse("User Id require*.",
                        HttpStatus.UNAUTHORIZED, "User Id Require");
            }
            
            return walletService.createWallet(request);
        } catch (Exception ex) {
            // Handle any unexpected errors
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to create wallet: " + ex.getMessage());
        }
    }

    @PostMapping("/set/pin")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> userSetTransferPin(@RequestBody TransactionRequest request,
            Authentication authentication) {
        String providedPin = request.getTransferpin();
        String token = tokenExtractor.extractToken(httpServletRequest);
        if (token.isBlank() || token.isEmpty()) {
            return Error.createResponse("UNAUTHORIZED*.",
                    HttpStatus.UNAUTHORIZED, "Require token to access this endpoint, Missing valid token.");
        }
        if (providedPin == null || providedPin.isEmpty()) {
            return Error.createResponse("Transfer pin is required.", HttpStatus.BAD_REQUEST,
                    "Please provide your transfer pin.");
        }
        return walletService.CreateUserTransferPin(request, token, authentication);
    }

    @GetMapping("/all/assets/balance")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getBalance(Authentication authentication) {
        String username = authentication.getName();
        String token = tokenExtractor.extractToken(httpServletRequest);
        if (token.isBlank() || token.isEmpty()) {
            return Error.createResponse("UNAUTHORIZED*.",
                    HttpStatus.UNAUTHORIZED, "Require token to access this endpoint, Missing valid token.");
        }
        return walletService.GetBalance(username, token);
    }

    @GetMapping("/balance/userId/{userId}/currency/{currency}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getBalanceByCurrencyType(@PathVariable String currency, @PathVariable Long userId) {
        String token = tokenExtractor.extractToken(httpServletRequest);
        if (token.isBlank() || token.isEmpty()) {
            return Error.createResponse("UNAUTHORIZED*.",
                    HttpStatus.UNAUTHORIZED, "Require token to access this endpoint, Missing valid token.");
        }

        if (currency.isBlank() || currency.isEmpty()) {
            return Error.createResponse("Currency Type is require*.",
                    HttpStatus.BAD_REQUEST, "Please provide paramenter Currency type.");
        }
        if (userId == null) {
            return Error.createResponse("User Id is require*.",
                    HttpStatus.BAD_REQUEST, "Please provide paramenter User id.");
        }

        if (!Arrays.stream(CurrencyType.values()).anyMatch(ct -> ct.name().equals(currency.toString().toUpperCase()))) {
            return Error.createResponse("Invalid Currency provided.*.",
                    HttpStatus.BAD_REQUEST,
                    "Please provide Currency type. any of this list (USD, EUR, NGN,GBP, JPY,AUD,CAD, CHF, CNY, OR INR)");
        }
        CurrencyType currencyType = CurrencyType.valueOf(currency.toString().toUpperCase());
        return walletService.GetBalanceByCurrencyType(userId, currencyType, token);
    }
    
    @GetMapping("/userId/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getByUserId(@PathVariable Long id) {
        return walletService.findByUserId(id);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getByWalletId(@PathVariable Long walletId) {
        return walletService.findById(walletId);
    }
    
    @PutMapping("/update")
    public ResponseEntity<?> updateUserWallet(HttpServletResponse response, @RequestBody UpdateWalletRequest request) {
        return walletService.updateUserWallet(request);
    }
    
    @PutMapping("/deduct/userId/{userId}")
    public ResponseEntity<?> deductUserWallet(HttpServletResponse response, @RequestBody DeductAmountRequest request) {

        return walletService.deductUserWallet(request);
    }
  
}
