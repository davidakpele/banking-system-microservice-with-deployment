package pesco.withdrawal_service.middleware;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;
import pesco.withdrawal_service.clients.BlackListServiceClient;
import pesco.withdrawal_service.clients.HistoryServiceClient;
import pesco.withdrawal_service.clients.UserServiceClient;
import pesco.withdrawal_service.dto.HistoryDTO;
import pesco.withdrawal_service.dto.UserDTO;
import pesco.withdrawal_service.enums.BanActions;
import pesco.withdrawal_service.enums.TransactionType;

@Component
public class UserTransactionsAgent {

    private final UserServiceClient userServiceClient;
    private final HistoryServiceClient historyServiceClient;
    private final BlackListServiceClient blackListServiceClient;
   
    public UserTransactionsAgent(UserServiceClient userServiceClient,
            HistoryServiceClient historyServiceClient, 
            BlackListServiceClient blackListServiceClient) {
        this.userServiceClient = userServiceClient;
        this.historyServiceClient = historyServiceClient;
        this.blackListServiceClient = blackListServiceClient;
    }

    private static final Set<String> HIGH_RISK_REGIONS = Set.of(
        "Philippines", "Venezuela", "Vietnam", "Yemen", "Haiti"
    );

    // Check if user has high volume or frequent transactions
    public boolean isHighVolumeOrFrequentTransactions(Long id, String token) {
        List<HistoryDTO> recentTransactions = historyServiceClient.FindRecentTransactionsByUserId(id,
                LocalDateTime.now().minusMinutes(10), token);
        BigDecimal totalAmount = recentTransactions.stream()
                .map(HistoryDTO::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Flag if more than 5 transactions in 10 minutes or total amount exceeds
        // threshold
        if (recentTransactions.size() > 5 || totalAmount.compareTo(new BigDecimal("100000000.00")) > 0) {
            // Send a request to block user wallet 
            blackListServiceClient.blockUserWallet(id, BanActions.MODERATOR_ACTION, token);
            return true;
        }
        return false;
    }
    
    // Check if account is new and performing high-risk transactions
    public boolean isNewAccountAndHighRisk(Long id, String token) {
        UserDTO user = userServiceClient.getUserById(id, token);
        if (user == null)
            return false;
        return !user.isEnabled() || user.getCreatedOn().isAfter(LocalDateTime.now().minusMinutes(3));
    }
   
    // Check for fraudulent activity (e.g., deposit followed by transfer)
    public boolean isFraudulentBehavior(Long id, String token) {
        List<HistoryDTO> recentTransactions = historyServiceClient.FindRecentTransactionsByUserId(id,
                LocalDateTime.now().minusHours(1), token);

        boolean hasDeposit = recentTransactions.stream()
                .anyMatch(tx -> tx.getType() == TransactionType.DEPOSIT);

        boolean hasTransfer = recentTransactions.stream()
                .anyMatch(tx -> tx.getType() == TransactionType.DEBITED);

        if (hasDeposit && hasTransfer) {
            // Trigger an alert or send a request to block user account and user wallet
            blackListServiceClient.blockUserWallet(id, BanActions.FRAUDULENT_ACTIVITY, token);
            userServiceClient.blockUserAccount(id, token); 
            return true;
        }
        return false;
    }

    // Check if user is from the black list users
    public boolean isFromBlacklistedAddress(Long id, String token) {
        Boolean exists = blackListServiceClient.FindByWalletId(id, token);
        return exists != null && exists;
    }

    // Transactions Involving High-Risk Regions
    public boolean isHighRiskRegion(String region) {
        return HIGH_RISK_REGIONS.contains(region);
    }

    // Inconsistent Behavior
    public boolean isInconsistentBehavior(Long userId, Long walletId, String token) {
        // Fetch recent transactions (e.g., last 24 hours)
        LocalDateTime minus24Hours = LocalDateTime.now().minusHours(24);
        List<HistoryDTO> recentTransactions = historyServiceClient.FindRecentTransactionsByUserId(userId, minus24Hours, token);

        if (recentTransactions.isEmpty()) {
            // If no recent transactions, behavior can't be inconsistent
            return false;
        }

        // Criteria 1: Check for sudden large transactions compared to the user's  typical behavior
        BigDecimal averageAmount = calculateAverageTransactionAmount(userId, walletId, token);
        for (HistoryDTO transaction : recentTransactions) {
            // threshold: if a transaction is more than 5 times the average amount
            if (transaction.getAmount().compareTo(averageAmount.multiply(BigDecimal.valueOf(5))) > 0) {
                return true;
            }
        }

        // Criteria 2: Check if there are too many transactions in a short period (e.g., > 10 in 24 hours)
        if (recentTransactions.size() > 10) {
            return true;
        }

        // Criteria 3: Check for transactions from different IP addresses (suggesting multiple locations or devices)
        String lastIpAddress = recentTransactions.get(0).getIpAddress();
        for (HistoryDTO transaction : recentTransactions) {
            if (!transaction.getIpAddress().equals(lastIpAddress)) {
                return true;
            }
        }

        // Criteria 4: Check for unusual transaction types (e.g., frequent withdrawals)
        long withdrawalCount = recentTransactions.stream()
                .filter(t -> t.getType() == TransactionType.WITHDRAW)
                .count();
        if (withdrawalCount > 5) {
            return true;
        }

        // If no inconsistencies detected
        return false;
    }

    // Helper method to calculate the average transaction amount for a user
    @SuppressWarnings("deprecation")
    private BigDecimal calculateAverageTransactionAmount(Long userId, Long walletId, String token) {
        List<HistoryDTO> allTransactions = historyServiceClient.FindByWalletIdAndUserId(userId, walletId, token);

        // Handle the case where there are no transactions
        if (allTransactions.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // Safely map and handle null amounts
        BigDecimal totalAmount = allTransactions.stream()
                .map(transaction -> transaction.getAmount() != null ? transaction.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Return the average transaction amount
        return totalAmount.divide(BigDecimal.valueOf(allTransactions.size()), BigDecimal.ROUND_HALF_UP);
    }


    
}
