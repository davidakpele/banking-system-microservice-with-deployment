package pesco.history_service.controllers;

import pesco.history_service.dtos.HistoryDTO;
import pesco.history_service.exceptions.Error;
import pesco.history_service.exceptions.UserClientNotFoundException;
import pesco.history_service.payloads.CreateWalletHistory;
import pesco.history_service.payloads.DeleteHistoryRequest;
import pesco.history_service.payloads.DepositHistoryRequest;
import pesco.history_service.payloads.FeaturesHistoryRequest;
import pesco.history_service.services.HistoryService;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import pesco.history_service.utils.TokenExtractor;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/history")
public class HistoryController {

    private final TokenExtractor tokenExtractor;
    private final HttpServletRequest httpServletRequest;
    private final HistoryService historyService;

    @GetMapping("/overview/currency/{currency}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> getHistoryByCurrencyType(@PathVariable String currency, Authentication authentication) {
        String username = authentication.getName();
        String token = tokenExtractor.ExtractToken(httpServletRequest);
        if (token.isBlank() || token.isEmpty()) {
            return Error.createResponse("UNAUTHORIZED*.",
                    HttpStatus.UNAUTHORIZED, "Require token to access this endpoint, Missing valid token.");
        }
        if (currency.isBlank() || currency.isEmpty()) {
            return Error.createResponse("Currency Type is require*.",
                    HttpStatus.BAD_REQUEST, "Please provide paramenter Currency type.");
        }
        return historyService.fetchUserHistoryByCurrencyType(currency, username, token);
    }

    @GetMapping("/overview/all")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> getAllHistory(Authentication authentication) {
        String username = authentication.getName();
        String token = tokenExtractor.ExtractToken(httpServletRequest);
        if (token.isBlank() || token.isEmpty()) {
            return Error.createResponse("UNAUTHORIZED*.", HttpStatus.UNAUTHORIZED, "Require token to access this endpoint, Missing valid token.");
        }
        return historyService.fetchAllUserHistory(username, token);
    }
    
    @PostMapping("/deposit/create")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> createDepositHistory(HttpServletRequest httpRequest, Authentication authentication, @RequestBody DepositHistoryRequest request) {
        String username = authentication.getName();
        String token = tokenExtractor.ExtractToken(httpServletRequest);
        if (token.isBlank() || token.isEmpty()) {
            return Error.createResponse("UNAUTHORIZED*.",
                    HttpStatus.UNAUTHORIZED, "Require token to access this endpoint, Missing valid token.");
        }
        if (request.getUserId() == null || request.getUserId()<=0) {
            return Error.createResponse("User Id require*.", HttpStatus.BAD_REQUEST,
                    "User Id require");
        }

        if (request.getCurrencyType() == null || request.getCurrencyType().toString().isEmpty()) {
            return Error.createResponse("Currency Type is require*.", HttpStatus.BAD_REQUEST,
                    "Currency Type is require");
        }
        if (request.getDescription() == null || request.getDescription().isEmpty()) {
            return Error.createResponse("Description is require*.", HttpStatus.BAD_REQUEST,
                    "Description is require");
        }

        if (request.getMessage() == null || request.getMessage().isEmpty()) {
            return Error.createResponse("Message is require*.", HttpStatus.BAD_REQUEST,
                    "Message is require");
        }
        return historyService.createDepositHistory(httpRequest, token, username, request);
    }

    @PostMapping("/withdraw/create")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> createWithdrawalHistory(HttpServletRequest httpRequest, Authentication authentication,
            @RequestBody CreateWalletHistory request) {
        String username = authentication.getName();
        String token = tokenExtractor.ExtractToken(httpServletRequest);
        if (token.isBlank() || token.isEmpty()) {
            return Error.createResponse("UNAUTHORIZED*.",
                    HttpStatus.UNAUTHORIZED, "Require token to access this endpoint, Missing valid token.");
        }

        return historyService.createWithdrawalHistory(httpRequest, token, username, request);
    }
    
    @PostMapping("/features/create")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> createFeaturessHistory(HttpServletRequest httpRequest, Authentication authentication, @RequestBody FeaturesHistoryRequest request){
        String username = authentication.getName();
        String token = tokenExtractor.ExtractToken(httpServletRequest);
        if (token.isBlank() || token.isEmpty()) {
            return Error.createResponse("UNAUTHORIZED*.",
                    HttpStatus.UNAUTHORIZED, "Require token to access this endpoint, Missing valid token.");
        }

        return historyService.createFeaturessHistory(httpRequest, token, username, request);
    }

    @DeleteMapping("/delete")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> deleteHistory(Authentication authentication, @RequestBody DeleteHistoryRequest request) {
        boolean subject = historyService.deleteHistory(request.getIds());
        return subject
                ? new ResponseEntity<>(HttpStatus.NO_CONTENT) // IF SUCCESSFUL
                : new ResponseEntity<>(HttpStatus.NOT_FOUND); // IF FAIL TO DELETE
    }
    
    @GetMapping("/wallet/{walletId}/transactions/timestamp")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<HistoryDTO>> findByTimestampAfterAndWalletId(
            @PathVariable Long walletId,
            @RequestParam Instant timestamp) {

        try {
            List<HistoryDTO> historyList = historyService.findByTimestampAfterAndWalletId(walletId, timestamp);

            if (historyList.isEmpty()) {
                return ResponseEntity.noContent().build();
            }

            return ResponseEntity.ok(historyList); 
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.emptyList());
        }
    }

    @PostMapping("/credit/create")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> createUserCreditHistory(HttpServletRequest httpRequest, Authentication authentication,
            @RequestBody CreateWalletHistory request) {
        String username = authentication.getName();
        String token = tokenExtractor.ExtractToken(httpServletRequest);
        if (token.isBlank() || token.isEmpty()) {
            return Error.createResponse("UNAUTHORIZED*.",
                    HttpStatus.UNAUTHORIZED, "Require token to access this endpoint, Missing valid token.");
        }
        System.out.println(request);
        
        return historyService.createUserCreditHistory(httpRequest, token, username, request);
    }

    @GetMapping("/wallet/{walletId}/userId/{userId}/transactions")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> findByWalletIdAndUserId(
            @PathVariable Long walletId,
            @PathVariable Long userId,
            @RequestHeader("Authorization") String token) {
        try {
            List<HistoryDTO> historyList = historyService.findByWalletIdAndUserId(walletId, userId);
            return ResponseEntity.ok(historyList);

        } catch (UserClientNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server error occurred.");
        }
    }

    @GetMapping("/{id}/transactions")
    public ResponseEntity<List<HistoryDTO>> findRecentTransactionsByUserId(
            @PathVariable Long id,
            @RequestParam("since") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since) {
        try {
            // Convert LocalDateTime to Instant (ensure correct timezone)
            Instant timestamp = since.atZone(ZoneId.systemDefault()).toInstant().truncatedTo(ChronoUnit.MILLIS);
            
            System.out.println("Converted Timestamp: " + timestamp);

            // Fetch data
            List<HistoryDTO> historyList = historyService.findRecentTransactionsByUserId(id, timestamp);
            
            System.out.println("Fetched Records: " + historyList.size());

            return ResponseEntity.ok(historyList);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }


    @GetMapping("/transactions/count")
    public ResponseEntity<?> countUserHistory(HttpServletRequest httpRequest, Authentication authentication) {
        try {
            String token = tokenExtractor.ExtractToken(httpRequest);
            if (token.isBlank() || token.isEmpty()) {
                return Error.createResponse("UNAUTHORIZED*.",
                        HttpStatus.UNAUTHORIZED, "Require token to access this endpoint, Missing valid token.");
            }
            
            return historyService.countUserHistory(token, authentication);
        } catch (UserClientNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }


}
