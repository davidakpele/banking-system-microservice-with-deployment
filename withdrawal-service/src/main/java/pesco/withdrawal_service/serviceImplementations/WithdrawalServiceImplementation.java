package pesco.withdrawal_service.serviceImplementations;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import pesco.withdrawal_service.clients.HistoryServiceClient;
import pesco.withdrawal_service.clients.NotificationServiceClient;
import pesco.withdrawal_service.clients.RevenueServiceClient;
import pesco.withdrawal_service.clients.UserServiceClient;
import pesco.withdrawal_service.clients.WalletServiceClient;
import pesco.withdrawal_service.dto.HistoryDTO;
import pesco.withdrawal_service.dto.UserDTO;
import pesco.withdrawal_service.dto.WalletDTO;
import pesco.withdrawal_service.dto.WithdrawHistoryRequestDTO;
import pesco.withdrawal_service.enums.BanActions;
import pesco.withdrawal_service.exceptions.Error;
import pesco.withdrawal_service.middleware.UserTransactionsAgent;
import pesco.withdrawal_service.enums.TransactionType;
import pesco.withdrawal_service.payloads.WithdrawInRequest;
import pesco.withdrawal_service.payloads.WithdrawOutRequest;
import pesco.withdrawal_service.services.WithdrawalService;
import pesco.withdrawal_service.utils.FeeCalculator;

@Service
@RequiredArgsConstructor
public class WithdrawalServiceImplementation implements WithdrawalService {
    
    private final HistoryServiceClient historyServiceClient;
    private final WalletServiceClient walletServiceClient;
    private final UserServiceClient userServiceClient;
    private final NotificationServiceClient notificationServiceClient;
    private final RevenueServiceClient revenueServiceClient;
    private final FeeCalculator feeCalculator;
    private final PasswordEncoder passwordEncoder;
    private final UserTransactionsAgent userTransactionsAgent;


    @Override
    public ResponseEntity<?> transferToExternalPlatform(String username, WithdrawOutRequest request,
            Authentication authentication, String token, HttpServletRequest httpRequest) {

        UserDTO fromUser = userServiceClient.getUserByUsername(username);

        if (fromUser == null) {
            return Error.createResponse("Sorry this user does not exist in our system.",
                    HttpStatus.BAD_REQUEST,
                    "Sender User does not exist, please provide valid username.");
        }

        if (fromUser != null && !fromUser.getUsername().equals(request.getSenderUser())) {
            return Error.createResponse(
                    "Fraudulent action is taken here, You are not the authorized user to operate this wallet.",
                    HttpStatus.FORBIDDEN,
                    "One more attempt from you again, you will be reported to the Economic and Financial Crimes Commission (EFCC).");
        }

        boolean isLockedAccount = fromUser.getRecords().get(0).isLocked();

        if (isLockedAccount) {
            return Error.createResponse(
                    "Your account has been temporarily locked. Please reach out to our support team to unlock your account.",
                    HttpStatus.FORBIDDEN,
                    "Please reach out to our support team to unlock your account.");
        }

        boolean isAccountBlocked = fromUser.getRecords().get(0).isBlocked();

        if (isAccountBlocked) {
            return Error.createResponse(
                    "Your account has been blocked due to security concerns. Contact our customer service for assistance with your blocked account.",
                    HttpStatus.FORBIDDEN,
                    "Contact our customer service for assistance with your blocked account.");
        }

        WalletDTO senderWalletAccount = walletServiceClient.findByUserId(fromUser.getId(), token);
        if (senderWalletAccount == null) {
            return Error.createResponse("Your wallet is not found", HttpStatus.BAD_REQUEST,
                    "Sorry.! Your wallet is not found.");
        }

        // Check for suspicious behaviors
        if (userTransactionsAgent.isHighVolumeOrFrequentTransactions(fromUser.getId(), token)) {
            return Error.createResponse("Account temporarily banned due to high volume of transactions.",
                    HttpStatus.FORBIDDEN,
                    "Please contact support.");
        }

        if (userTransactionsAgent.isNewAccountAndHighRisk(fromUser.getId(), token)) {
            return Error.createResponse("Account temporarily banned due to unverified or newly created wallet.",
                    HttpStatus.FORBIDDEN,
                    "Please contact support.");
        }

        if (userTransactionsAgent.isFraudulentBehavior(fromUser.getId(), token)) {
            return Error.createResponse(
                    "Fraudulent Activity Detected",
                    HttpStatus.FORBIDDEN,
                    "Your account has been flagged for suspicious activity. Please contact support immediately.");
        }

        if (userTransactionsAgent.isFromBlacklistedAddress(senderWalletAccount.getId(), token)) {
            return Error.createResponse("Transaction blocked due to blacklisted wallet address.", HttpStatus.FORBIDDEN,
                    "Please contact support.");
        }

        // Verify user transfer pin password
        String providedPin = request.getTransferpin().trim();

        if (providedPin == null || providedPin.isEmpty()) {
            return Error.createResponse("Transfer pin is required.", HttpStatus.BAD_REQUEST,
                    "Please provide your transfer pin.");
        }

        if (!passwordEncoder.matches(providedPin, senderWalletAccount.getPassword())) {
            return Error.createResponse("Invalid transfer pin.", HttpStatus.UNAUTHORIZED,
                    "The provided transfer pin is incorrect.");
        }

        List<HistoryDTO> activities = historyServiceClient.FindByTimestampAfterAndWalletId(
                Instant.now().minus(1, ChronoUnit.MINUTES), senderWalletAccount.getId(), token);
        // detect suspicious patterns
        for (HistoryDTO activity : activities) {
            if (activity.getType() == TransactionType.DEPOSIT
                    && activity.getTimestamp().after(Timestamp.from((Instant.now().minus(1, ChronoUnit.MINUTES))))) {
                // update user account status
                userServiceClient.updateUserAccountStatus(fromUser.getId(), BanActions.SUSPICIOUS_ACTIVITY, token);
            }
        }
        // get platform fee amount
        BigDecimal feePercentage = feeCalculator.calculateFee(request.getAmount());
        // Calculate the fee based on the transfer amount
        BigDecimal feeAmount = request.getAmount().multiply(feePercentage);
        // Calculate the total deduction (amount + fee)
        BigDecimal finalDeduction = feeAmount.add(request.getAmount());

        Map<String, Object> response = new HashMap<>();

        try {
            BigDecimal balance = walletServiceClient.FetchUserBalance(fromUser.getId(), request.getCurrencyType().toString(), token);
            if (((BigDecimal) balance).compareTo(finalDeduction) < 0) {
                return Error.createResponse("Insufficient balance", HttpStatus.BAD_REQUEST,
                        "Your account balance is low.");
            }

            // ELSE:

            /**
             * a) Send request to api gateway either paystack, flutterwave depending the
             * user choosen.
             * b) Send PUT request to wallet-service to substract / deduct the amount from
             * user wallet.
             * c) Send POST request Record user history .
             * d) Send notification to user email about the action just process "DEBIT
             * ALERT"
             * e) Return successful.
             *
             * NOTE: Use CompletableFuture.runAsync(()) in a, b, c
             */
            response.put("message", "Withdraw Successful.!");
            response.put("details",
                    "The " + request.getCurrencyType() + " " + request.getAmount() + " Withdraw was successful.");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("message", "Transaction Fail.!");
            response.put("details", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_GATEWAY);
        }

    }

    @Override
    public ResponseEntity<?> WithdrawUsingUsernameToUserInSamePlatform(String username, WithdrawInRequest request,
            Authentication authentication, String token, HttpServletRequest httpRequest) {
        try {
            UserDTO fromUser = userServiceClient.getUserByUsername(username);
            UserDTO toUser = userServiceClient.getUserByUsername(request.getRecipientUser());
       
            if (fromUser == null) {
                return Error.createResponse("Sender user does not exist, please provide a valid username.",
                        HttpStatus.BAD_REQUEST,
                        "Sender user does not exist, please provide a valid username.");
            }

            if (fromUser != null && !fromUser.getUsername().equals(request.getSenderUser())) {
                return Error.createResponse(
                        "Fraudulent action detected. You are not authorized to operate this wallet.",
                        HttpStatus.FORBIDDEN,
                        "One more attempt and you will be reported to the Economic and Financial Crimes Commission (EFCC).");
            }
            
            boolean isLockedAccount = fromUser.getRecords().get(0).isLocked();
            boolean isBlockedAccount = fromUser.getRecords().get(0).isBlocked();

            if (isLockedAccount) {
                return Error.createResponse(
                        "Your account has been temporarily locked. Please contact support.",
                        HttpStatus.FORBIDDEN, "Please contact support to unlock your account.");
            }

            if (isBlockedAccount) {
                return Error.createResponse(
                        "Your account has been temporarily banned. Please contact support.",
                        HttpStatus.FORBIDDEN, "Please contact support to verify your account.");
            }

            if (toUser == null) {
                return Error.createResponse("Receiver not found", HttpStatus.BAD_REQUEST,
                        "The recipient username does not exist in our system.");
            }
            
            WalletDTO senderWalletAccount = walletServiceClient.findByUserId(fromUser.getId(), token);
            if (senderWalletAccount == null) {
                return Error.createResponse("Your wallet is not found", HttpStatus.BAD_REQUEST,
                        "Sorry.! Your wallet is not found.");
            }
           
            // Check suspicious behavior
            if (userTransactionsAgent.isHighVolumeOrFrequentTransactions(fromUser.getId(), token)) {
                return Error.createResponse("Account temporarily banned due to suspicious activity.",
                        HttpStatus.FORBIDDEN,
                        "Account temporarily banned due to suspicious activity. Please contact support.");
            }

            if (userTransactionsAgent.isHighRiskRegion(request.getRegion())) {
                return Error.createResponse(
                        "Access Restricted",
                        HttpStatus.FORBIDDEN,
                        "Your account has been temporarily restricted due to activity from a high-risk region. " +
                                "Please contact support for assistance.");
            }

            if (userTransactionsAgent.isNewAccountAndHighRisk(fromUser.getId(), token)) {
                return Error.createResponse(
                        "New Account Restrictions",
                        HttpStatus.FORBIDDEN,
                        "New accounts have transaction limits for security reasons. Please verify your identity to continue.");
            }
            if (userTransactionsAgent.isInconsistentBehavior(fromUser.getId(), fromUser.getId(), token)) {
                return Error.createResponse(
                        "Suspicious Activity Detected",
                        HttpStatus.FORBIDDEN,
                        "Unusual transaction activity has been detected on your account. Please contact support.");
            }

            if (userTransactionsAgent.isFraudulentBehavior(fromUser.getId(), token)) {
                return Error.createResponse(
                        "Fraudulent Activity Detected",
                        HttpStatus.FORBIDDEN,
                        "Your account has been flagged for suspicious activity. Please contact support immediately.");
            }
            List<HistoryDTO> activities = historyServiceClient.FindByTimestampAfterAndWalletId(
                    Instant.now().minus(1, ChronoUnit.MINUTES), fromUser.getId(), token);
            // detect suspicious patterns
            if (activities != null) {
                for (HistoryDTO activity : activities) {
                    if (activity.getType() == TransactionType.DEPOSIT
                            && activity.getTimestamp()
                                    .after(Timestamp.from((Instant.now().minus(1, ChronoUnit.MINUTES))))) {
                        // update user account status
                        userServiceClient.updateUserAccountStatus(fromUser.getId(), BanActions.SUSPICIOUS_ACTIVITY,
                                token);
                    }
                }
            }
            // Verify transfer pin
            String providedPin = request.getTransferpin().trim();
            if (providedPin == null || providedPin.isEmpty()) {
                return Error.createResponse("Transfer pin is required.", HttpStatus.BAD_REQUEST,
                        "Please provide your transfer pin.");
            }
            
            if (!passwordEncoder.matches(providedPin, senderWalletAccount.getPassword())) {
                return Error.createResponse("Invalid transfer pin.", HttpStatus.UNAUTHORIZED,
                        "The provided transfer pin is incorrect.");
            }
            // Calcuate platform percentage
            BigDecimal feePercentage = feeCalculator.calculateFee(request.getAmount());
            // Calculating platform profit
            BigDecimal feeAmount = request.getAmount().multiply(feePercentage);
            // Calculate the final amount that will be deducted from sender user wallet
            BigDecimal finalDeduction = feeAmount.add(request.getAmount());
            
            BigDecimal senderWalletBalance =walletServiceClient.FetchUserBalance(fromUser.getId(), request.getCurrencyType().toString(), token);
            if (senderWalletBalance.compareTo(finalDeduction) < 0) {
                return Error.createResponse("Insufficient balance", HttpStatus.BAD_REQUEST,
                        "Your account balance is low.");
            }

            WalletDTO recipientWalletAccount = walletServiceClient.findByUserId(toUser.getId(), token);
            // Handle recipient wallet creation if it doesn't exist
            if (recipientWalletAccount == null) {
                // Just in case recipient user wallet is not found or null, the create and credit the wallet.
                CompletableFuture<Void> createNewWallet = CompletableFuture.runAsync(() -> walletServiceClient.createUserWallet(toUser.getId()));
                createNewWallet.join();
            }
        
            // Deduct amount from sender wallet
            CompletableFuture<Void>deductFromSenderWallet = CompletableFuture.runAsync(() -> walletServiceClient.DeductAmountFromSenderWallet(fromUser.getId(), request.getCurrencyType(), token, finalDeduction));
            deductFromSenderWallet.join();

            // Credit recipient user wallet
            CompletableFuture<Void> UpdateCreatedWallet = CompletableFuture.runAsync(() -> walletServiceClient.updateUserWallet(toUser.getId(), request.getAmount(), request.getCurrencyType(), token));
            UpdateCreatedWallet.join();

            String senderIp = walletServiceClient.getClientIp(httpRequest); 
            String recipientIp = senderIp;
            // Create history for both users.
            WithdrawHistoryRequestDTO senderHistory = new WithdrawHistoryRequestDTO();
            senderHistory.setAmount(request.getAmount().negate());
            senderHistory.setCurrencyType(request.getCurrencyType());
            senderHistory.setDescription("DEPITED");
            senderHistory.setMessage("Transfer "+ senderWalletAccount.getBalances().get(0).getCurrencySymbol().toString()+request.getAmount()+" to "+ toUser.getUsername());
            senderHistory.setIp_address(senderIp);
            senderHistory.setType(TransactionType.DEBITED);
            senderHistory.setRecipientUsername(toUser.getUsername());
            senderHistory.setUserId(fromUser.getId());
            senderHistory.setSenderUsername(fromUser.getUsername());
            senderHistory.setWalletId(senderWalletAccount.getId());
            
            CompletableFuture<Void>CreateSenderHistory = CompletableFuture.runAsync(() -> historyServiceClient.createUserCreditHistory(senderHistory, token));
            CreateSenderHistory.join();

            WithdrawHistoryRequestDTO recipientHistory = new WithdrawHistoryRequestDTO();
            recipientHistory.setAmount(request.getAmount());
            recipientHistory.setCurrencyType(request.getCurrencyType());
            recipientHistory.setDescription("CREDITED");
            recipientHistory.setMessage("Credited "+senderWalletAccount.getBalances().get(0).getCurrencySymbol().toString()+request.getAmount()+" by "+ fromUser.getUsername());
            recipientHistory.setIp_address(recipientIp);
            recipientHistory.setType(TransactionType.CREDITED);
            recipientHistory.setRecipientUsername(toUser.getUsername());
            recipientHistory.setUserId(toUser.getId());
            recipientHistory.setSenderUsername(fromUser.getUsername());
            recipientHistory.setWalletId((recipientWalletAccount !=null ? recipientWalletAccount.getId() : null));
           
            CompletableFuture<Void>CreateRecipientHistory = CompletableFuture.runAsync(() -> historyServiceClient.createUserCreditHistory(recipientHistory, token));
            CreateRecipientHistory.join();

            CompletableFuture<Void>addRevenue = CompletableFuture.runAsync(() -> revenueServiceClient.addToPlatformRevenue(feeAmount, request.getCurrencyType()));
            addRevenue.join();

            BigDecimal senderNewWalletBalance =walletServiceClient.FetchUserBalance(fromUser.getId(), request.getCurrencyType().toString(), token);
            CompletableFuture<Void>sendDebitAlert = CompletableFuture.runAsync(() -> notificationServiceClient.sendDebitAlert(fromUser.getEmail(), fromUser.getUsername(), toUser.getUsername(), request.getAmount(), request.getCurrencyType().toString(), feeAmount, senderNewWalletBalance));
            sendDebitAlert.join();

            BigDecimal recieverNewWalletBalance =walletServiceClient.FetchUserBalance(toUser.getId(), request.getCurrencyType().toString(), token);
            CompletableFuture<Void>sendCreditAlert = CompletableFuture.runAsync(() -> notificationServiceClient.sendCreditAlert(fromUser.getUsername(), toUser.getEmail(), toUser.getUsername(), request.getAmount(), request.getCurrencyType().toString(), recieverNewWalletBalance));
            sendCreditAlert.join();

            Map<String, Object> response = new HashMap<>();

            response.put("message", "Transaction successful");
            response.put("details", request.getAmount() + " has been successfully sent to " + request.getRecipientUser() + ".");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception ex) {
            return Error.createResponse("An unexpected error occurred.", HttpStatus.INTERNAL_SERVER_ERROR,
                    ex.getMessage());
        }
    }
     

    
}
