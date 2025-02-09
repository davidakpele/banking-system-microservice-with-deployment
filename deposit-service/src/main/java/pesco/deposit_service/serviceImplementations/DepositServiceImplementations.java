package pesco.deposit_service.serviceImplementations;


import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import pesco.deposit_service.clients.BankListServiceClient;
import pesco.deposit_service.clients.FlutterWaveServiceClient;
import pesco.deposit_service.clients.HistoryServiceClient;
import pesco.deposit_service.clients.NotificationServiceClient;
import pesco.deposit_service.clients.PayStackServiceClient;
import pesco.deposit_service.clients.UserServiceClient;
import pesco.deposit_service.clients.WalletServiceClient;
import pesco.deposit_service.dto.BankListDTO;
import pesco.deposit_service.dto.UserDTO;
import pesco.deposit_service.dto.WalletDTO;
import pesco.deposit_service.enums.DEPOSITANDWITHDRAWALSYSTEM;
import pesco.deposit_service.enums.TransactionStatus;
import pesco.deposit_service.enums.TransactionType;
import pesco.deposit_service.payloads.DepositHistoryRequest;
import pesco.deposit_service.payloads.DepositRequest;
import pesco.deposit_service.services.DepositService;
import pesco.deposit_service.exceptions.BanKDetailsNotFound;
import pesco.deposit_service.exceptions.Error;

@Service
@RequiredArgsConstructor
public class DepositServiceImplementations implements DepositService {

    private final WalletServiceClient walletServiceClient;
    private final BankListServiceClient bankListServiceClient;
    private final UserServiceClient userServiceClient;
    private final PayStackServiceClient payStackServiceClient;
    private final HistoryServiceClient historyServiceClient;
    private final NotificationServiceClient notificationServiceWebClient;
    
    @SuppressWarnings("unused")
    private final FlutterWaveServiceClient flutterWaveServiceClient;

   @Override
    public ResponseEntity<?> createDeposit(DepositRequest request, String token, Authentication authentication) {
        String username = authentication.getName();
        UserDTO user = userServiceClient.getUserByUsername(username, token);
        Map<String, Object> response = new HashMap<>();

        try {
            // Fetch user bank details
            BankListDTO bankDetails = bankListServiceClient.findByAccountNumber(request.getAccountNumber(), token);

            // Validate bank details
            if (bankDetails != null &&
                    !user.getId().equals(request.getUserId())) {

                return Error.createResponse(
                        "Invalid Bank Details",
                        HttpStatus.FORBIDDEN,
                        "The bank account details provided do not match any record in the system.");
            }
            WalletDTO wallet = walletServiceClient.findByUserId(request.getUserId(), token);
            if (!wallet.getUserId().equals(request.getUserId())) {
                return Error.createResponse(
                        "Fraudulent Attempt Detected",
                        HttpStatus.FORBIDDEN,
                        "The wallet does not belong to you.");
            }
            // Handle deposit processing
            if (request.getDepositSystem() == DEPOSITANDWITHDRAWALSYSTEM.FLUTTERWAVE) {
                return processFlutterWaveDeposit(request, user, wallet.getUserId(), token);
            } else if (request.getDepositSystem() == DEPOSITANDWITHDRAWALSYSTEM.PAYSTACK) {
                return processPaystackDeposit(request, user, wallet.getUserId(), token);
            }

            // If deposit system is neither Flutterwave nor Paystack
            return Error.createResponse(
                    "Fraudulent Attempt Detected",
                    HttpStatus.FORBIDDEN,
                    "The bank account does not belong to you. Multiple attempts may lead to account suspension."
            );

        } catch (BanKDetailsNotFound e) {
            response.put("message", e.getMessage());
            response.put("details", e.getDetails());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

        } catch (JsonProcessingException e) {
            response.put("message", "Payment processing error");
            response.put("details", "Failed to initialize payment.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);

        } catch (Exception e) {
            response.put("error", "An unexpected error occurred");
            response.put("details", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    private ResponseEntity<?> processPaystackDeposit(DepositRequest request, UserDTO user, Long walletId,String token)
            throws JsonProcessingException {
        Map<String, Object> response = new HashMap<>();

        // Initialize payment with Paystack
        String paymentResponse = payStackServiceClient
                .initializePayment(user.getEmail(), request.getAmount().intValue() * 100)
                .block();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(paymentResponse);
        String authorizationUrl = rootNode.path("data").path("authorization_url").asText();

        // Credit user wallet asynchronously
        CompletableFuture.runAsync(() -> walletServiceClient.creditUserWallet(request.getAmount(), request.getCurrencyType(), request.getUserId(), token))
            .join();

        // Create transaction history
        DepositHistoryRequest depositHistoryRequest = new DepositHistoryRequest();
        depositHistoryRequest.setAmount(request.getAmount());
        depositHistoryRequest.setCurrencyType(request.getCurrencyType());
        depositHistoryRequest.setDescription(TransactionStatus.Success.toString());
        depositHistoryRequest.setType(TransactionType.DEPOSIT);
        depositHistoryRequest.setIp_address("");
        depositHistoryRequest.setMessage("Deposited " +request.getAmount()+" into your "+request.getCurrencyType()+" wallet.");
        depositHistoryRequest.setUserId(request.getUserId());
        depositHistoryRequest.setWalletId(walletId);

        CompletableFuture.runAsync(() -> historyServiceClient.createDepositHistory(depositHistoryRequest, token)).join();
        
        BigDecimal recieverNewWalletBalance =walletServiceClient.FetchUserBalance(user.getId(), request.getCurrencyType().toString(), token);
        CompletableFuture.runAsync(() -> notificationServiceWebClient.sendDepositNotification(user.getEmail(), user.getUsername(), request.getAmount(), request.getCurrencyType(), recieverNewWalletBalance))
        .join();
        
        response.put("url", authorizationUrl);
        response.put("details", "Please complete your transaction.");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private ResponseEntity<?> processFlutterWaveDeposit(DepositRequest request, UserDTO user,Long walletId, String token) {
        Map<String, Object> response = new HashMap<>();
        // Credit user wallet asynchronously
        
        CompletableFuture.runAsync(() -> walletServiceClient.creditUserWallet(request.getAmount(), request.getCurrencyType(), request.getUserId(), token))
            .join();

        // Create transaction history
        DepositHistoryRequest depositHistoryRequest = new DepositHistoryRequest();
        depositHistoryRequest.setAmount(request.getAmount());
        depositHistoryRequest.setCurrencyType(request.getCurrencyType());
        depositHistoryRequest.setDescription(TransactionStatus.Success.toString());
        depositHistoryRequest.setType(TransactionType.DEPOSIT);
        depositHistoryRequest.setIp_address("");
        depositHistoryRequest.setMessage("Deposited " +request.getAmount()+" into your "+request.getCurrencyType()+" wallet.");
        depositHistoryRequest.setUserId(request.getUserId());
        depositHistoryRequest.setWalletId(walletId);

        CompletableFuture.runAsync(() -> historyServiceClient.createDepositHistory(depositHistoryRequest, token)).join();

        BigDecimal recieverNewWalletBalance =walletServiceClient.FetchUserBalance(user.getId(), request.getCurrencyType().toString(), token);
        CompletableFuture.runAsync(() -> notificationServiceWebClient.sendDepositNotification(user.getEmail(), user.getUsername(), request.getAmount(), request.getCurrencyType(), recieverNewWalletBalance))
        .join();

        response.put("message", "Deposit Successful.!");
        response.put("details", request.getAmount()+" Deposited into "+request.getCurrencyType()+" wallet");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    
}
