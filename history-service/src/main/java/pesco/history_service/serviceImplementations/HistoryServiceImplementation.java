package pesco.history_service.serviceImplementations;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpServletRequest;
import pesco.history_service.exceptions.Error;
import lombok.RequiredArgsConstructor;
import pesco.history_service.clients.UserServiceClient;
import pesco.history_service.clients.WalletServiceClient;
import pesco.history_service.dtos.HistoryDTO;
import pesco.history_service.dtos.UserDTO;
import pesco.history_service.dtos.WalletDTO;
import pesco.history_service.dtos.WalletDTO.BalanceDTO;
import pesco.history_service.enums.CurrencyType;
import pesco.history_service.enums.TransactionStatus;
import pesco.history_service.enums.TransactionType;
import pesco.history_service.models.History;
import pesco.history_service.payloads.CreateWalletHistory;
import pesco.history_service.payloads.DepositHistoryRequest;
import pesco.history_service.payloads.FeaturesHistoryRequest;
import pesco.history_service.repository.HistoryRepository;
import pesco.history_service.responses.AccountOverView;
import pesco.history_service.responses.HistoryRepones;
import pesco.history_service.services.HistoryService;
import pesco.history_service.utils.KeysWrapper;

@Service
@RequiredArgsConstructor 
public class HistoryServiceImplementation implements HistoryService{

    private final UserServiceClient userServiceClient;
    private final HistoryRepository historyRepository;
    private final WalletServiceClient walletServiceClient;
    private final KeysWrapper keywrapper;

    @Override
    public ResponseEntity<?> fetchUserHistoryByCurrencyType(String currency, String username, String token) {
        // Check if user exists
        UserDTO user = userServiceClient.getUserByUsername(username, token);
        if (user == null) {
            return Error.createResponse("User not found", HttpStatus.BAD_REQUEST,
                    "User does not exists in our sysetm.");
        }

       // Get Wallet information from wallet-service
        WalletDTO walletDTO = walletServiceClient.getWalletByUser(user, token);
        if (walletDTO == null) {
            return Error.createResponse("Wallet not found", HttpStatus.BAD_REQUEST,
                    "We couldn't find wallet registered under this user.");
        }
        // Check if currency is valid
        if (!Arrays.stream(CurrencyType.values()).anyMatch(ct -> ct.name().equals(currency.toString().toUpperCase()))) {
            return Error.createResponse("Invalid Currency provided*.",
                    HttpStatus.BAD_REQUEST,
                    "Please provide Currency type. any of this list (USD, EUR, NGN, GBP, JPY, AUD, CAD, CHF, CNY, OR INR)");
        }

        CurrencyType currencyType = CurrencyType.valueOf(currency.toString().toUpperCase());
        BalanceDTO balanceDTO = walletDTO.getBalances().stream()
        .filter(balance -> balance.getCurrencyCode().equals(currencyType.name()))
        .findFirst()
                .orElse(null);

        if (balanceDTO == null) {
            return Error.createResponse("Balance not found for currency", HttpStatus.BAD_REQUEST,
                    "We couldn't find balance for the specified currency.");
        }
       // Check if transactions exist
        List<History> transactions = historyRepository.findByWalletAndCurrency(walletDTO.getId(), currencyType);
        if (transactions.isEmpty()) {
            return ResponseEntity.status(HttpStatus.OK).body(Collections.emptyList());
        }

        // Convert transactions to HistoryResponse
        List<HistoryRepones> historyReponess = transactions.stream()
            .map(transaction -> {
                HistoryRepones response = new HistoryRepones();
                response.setId(transaction.getId());
                response.setWalletId(walletDTO.getId());
                response.setSessionId(transaction.getSessionId());
                response.setAmount(FormatBigDecimal(transaction.getAmount()));
                response.setType(transaction.getType());
                response.setDescription(transaction.getDescription());
                response.setMessage(transaction.getMessage());
                response.setStatus(transaction.getStatus());
                response.setCreatedOn(transaction.getCreatedOn());
                return response;
        }).collect(Collectors.toList());

        String balance = FormatBigDecimal(balanceDTO.getBalance());
        AccountOverView accountOverview = new AccountOverView();
        accountOverview.setId(user.getId());
        accountOverview.setBalance(balance);
        accountOverview.setTransactions(historyReponess);
        return ResponseEntity.ok(accountOverview);
    }

    @Override
    public ResponseEntity<?> fetchAllUserHistory(String username, String token) {
        // Check if user exists
        UserDTO user = userServiceClient.getUserByUsername(username, token);
        if (user == null) {
            return Error.createResponse("User not found", HttpStatus.BAD_REQUEST,
                    "User does not exists in our sysetm.");
        }

        // Get Wallet information from wallet-service
        WalletDTO walletDTO = walletServiceClient.getWalletByUser(user, token);
        if (walletDTO == null) {
            return Error.createResponse("Wallet not found", HttpStatus.BAD_REQUEST,
                    "We couldn't find wallet registered under this user.");
        }

        // Check if transactions exist
        List<History> transactions = historyRepository.findByWalletIdAndUserId(walletDTO.getId(), user.getId());
        if (transactions.isEmpty()) {
            return ResponseEntity.status(HttpStatus.OK).body(Collections.emptyList());
        }

        // Convert transactions to HistoryResponse
        List<HistoryRepones> historyReponess = transactions.stream()
                .map(transaction -> {
                    HistoryRepones response = new HistoryRepones();
                    response.setId(transaction.getId());
                    response.setWalletId(walletDTO.getId());
                    response.setSessionId(transaction.getSessionId());
                    response.setAmount(FormatBigDecimal(transaction.getAmount()));
                    response.setType(transaction.getType());
                    response.setDescription(transaction.getDescription());
                    response.setMessage(transaction.getMessage());
                    response.setStatus(transaction.getStatus());
                    response.setCreatedOn(transaction.getCreatedOn());
                    return response;
                }).collect(Collectors.toList());

        AccountOverView accountOverview = new AccountOverView();
        accountOverview.setId(user.getId());
        accountOverview.setBalance(null);
        accountOverview.setTransactions(historyReponess);
        return ResponseEntity.ok(accountOverview);
    }

    @Override
    public ResponseEntity<?> createDepositHistory(HttpServletRequest httpRequest, String token, String username, DepositHistoryRequest request) {
        UserDTO user = userServiceClient.getUserByUsername(username, token);
        if (user == null) {
            return Error.createResponse("User not found", HttpStatus.BAD_REQUEST,
                    "User does not exists in our sysetm.");
        }
        // Generate Session key
        String sessionId = keywrapper.generateSessionId().toString();
    
        String formattedTransactionType = formatEnumValue(TransactionType.DEPOSIT);
        String ipAddress = httpRequest.getRemoteAddr();
        History userHistory = new History();
        userHistory.setId(keywrapper.createSnowflakeUniqueId());
        userHistory.setUserId(request.getUserId());
        userHistory.setWalletId(request.getWalletId());
        userHistory.setSessionId(sessionId);
        userHistory.setAmount(request.getAmount());
        userHistory.setType(request.getType());
        userHistory.setCurrencyType(request.getCurrencyType());
        userHistory.setIpAddress(ipAddress);
        userHistory.setTimestamp(Timestamp.from(Instant.now()));
        userHistory.setMessage("Deposite " +request.getCurrencyType()+ " " +request.getAmount()+ " INTO YOUR "+request.getCurrencyType()+" ACCOUNT");
        userHistory.setStatus(TransactionStatus.Success.toString());
        userHistory.setDescription(formattedTransactionType);
    
        try {
            historyRepository.save(userHistory);
            return Error.createResponse("Successfully created history", HttpStatus.OK,
                    "Successfully Create history");
        } catch (Exception e) {
            return Error.createResponse("Failed to process history.", HttpStatus.CONFLICT,
                    "details "+e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> createFeaturessHistory(HttpServletRequest httpRequest, String token, String username, FeaturesHistoryRequest request) {
        UserDTO user = userServiceClient.getUserByUsername(username, token);
        if (user == null) {
            return Error.createResponse("User not found", HttpStatus.BAD_REQUEST,
                    "User does not exists in our sysetm.");
        }
        String ipAddress = httpRequest.getRemoteAddr();
        // Generate Session key
        String sessionId = keywrapper.generateSessionId().toString();

        String formattedTransactionType = formatEnumValue(request.getType());

        History userHistory = new History();
        userHistory.setId(keywrapper.createSnowflakeUniqueId());
        userHistory.setUserId(request.getUserId());
        userHistory.setWalletId(request.getWalletId());
        userHistory.setSessionId(sessionId);
        userHistory.setAmount(request.getAmount());
        userHistory.setType(request.getType());
        userHistory.setIpAddress(ipAddress);
        userHistory.setCurrencyType(request.getCurrencyType());
        userHistory.setMessage(formattedTransactionType + request.getCurrencyType() + request.getAmount() + " INTO YOUR "
                + request.getCurrencyType());
        userHistory.setStatus(TransactionStatus.Success.toString());
        userHistory.setDescription(formattedTransactionType);

        try {
            historyRepository.save(userHistory);
            return Error.createResponse("Successfully created history", HttpStatus.OK,
                    "Successfully Create history");
        } catch (Exception e) {
            return Error.createResponse("Failed to process history.", HttpStatus.CONFLICT,
                    "details " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> createWithdrawalHistory(HttpServletRequest httpRequest, String token, String username, 
            CreateWalletHistory request) {
        UserDTO senderUser = userServiceClient.getUserByUsername(username, token);
        if (senderUser == null) {
            return Error.createResponse("Sender User not found", HttpStatus.BAD_REQUEST,
                    "Sender User does not exists in our sysetm.");
        }

        UserDTO recipientUser = userServiceClient.findUserByUsernameInPublicRoute(request.getRecipientUsername());
        if (recipientUser == null) {
            return Error.createResponse("Recipient User not found", HttpStatus.BAD_REQUEST,
                    "Recipient User does not exists in our sysetm.");
        }
        // Generate Session key
        String s1 = keywrapper.generateSessionId().toString();
        String ipAddress = httpRequest.getRemoteAddr();
        String formattedTransactionType = formatEnumValue(request.getType());

        History senderHistory = new History();
        senderHistory.setId(keywrapper.createSnowflakeUniqueId());
        senderHistory.setWalletId(request.getWalletId());
        senderHistory.setUserId(request.getUserId());
        senderHistory.setSessionId(s1);
        senderHistory.setAmount(request.getAmount().negate());
        senderHistory.setType(request.getType());
        senderHistory.setIpAddress(ipAddress);
        senderHistory.setCurrencyType(request.getCurrencyType());
        senderHistory.setMessage("TRANSFERED " + request.getCurrencyType() + request.getAmount() + " TO " + recipientUser.getUsername());
        senderHistory.setStatus(TransactionStatus.Success.toString());
        senderHistory.setDescription(formattedTransactionType);

        String s2 = keywrapper.generateSessionId().toString();

        History receiverHistory = new History();
        receiverHistory.setId(keywrapper.createSnowflakeUniqueId());
        receiverHistory.setWalletId(request.getWalletId());
        receiverHistory.setUserId(request.getUserId());
        receiverHistory.setSessionId(s2);
        receiverHistory.setAmount(request.getAmount());
        receiverHistory.setType(request.getType());
        receiverHistory.setIpAddress(ipAddress);
        receiverHistory.setCurrencyType(request.getCurrencyType());
        receiverHistory.setMessage("TRANSFER "  +request.getCurrencyType() +request.getAmount()+ " FROM " + senderUser.getUsername());
        receiverHistory.setStatus(TransactionStatus.Success.toString());
        receiverHistory.setDescription(formattedTransactionType);
        try {
            historyRepository.save(senderHistory);
            historyRepository.save(receiverHistory);
            return Error.createResponse("Successfully created history", HttpStatus.OK,
                    "Successfully Create history");
        } catch (Exception e) {
            return Error.createResponse("Failed to process history.", HttpStatus.CONFLICT,
                    "details " + e.getMessage());
        }
    }

    private static String FormatBigDecimal(BigDecimal value) {
        String pattern = "#,##0.00";
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator(',');
        symbols.setDecimalSeparator('.');
        DecimalFormat decimalFormat = new DecimalFormat(pattern, symbols);
        return decimalFormat.format(value);
    }

    private static String formatEnumValue(TransactionType transactionType) {
        return transactionType.name().replace("_", " ");
    }

    @Override
    public boolean deleteHistory(List<Long> ids) {
        int rowsAffected = historyRepository.deleteByIdIn(ids);
        return rowsAffected > 0;
    }

    @Override
    public List<HistoryDTO> findByTimestampAfterAndWalletId(Long walletId, Instant timestamp) {
        Timestamp sqlTimestamp = Timestamp.from(timestamp);
        List<History> historyList = historyRepository.findByTimestampAfterAndWalletId(sqlTimestamp, walletId);

        // Convert to DTOs
        return historyList.stream()
                .map(history -> new HistoryDTO(history))
                .collect(Collectors.toList());
    }

    @Override
    public ResponseEntity<?> createUserCreditHistory(HttpServletRequest httpRequest, String token, String username,
            CreateWalletHistory request) {
        UserDTO user = userServiceClient.getUserByUsername(username, token);
        if (user == null) {
            return Error.createResponse("User not found", HttpStatus.BAD_REQUEST,
                    "User does not exists in our sysetm.");
        }
        // Generate Session key
        String sessionId = keywrapper.generateSessionId().toString();

        String formattedTransactionType = formatEnumValue(request.getType());
        String ipAddress = httpRequest.getRemoteAddr();
        History userHistory = new History();
        userHistory.setId(keywrapper.createSnowflakeUniqueId());
        userHistory.setUserId(request.getUserId());
        userHistory.setWalletId(request.getWalletId());
        userHistory.setSessionId(sessionId);
        userHistory.setAmount(request.getAmount());
        userHistory.setType(request.getType());
        userHistory.setCurrencyType(request.getCurrencyType());
        userHistory.setIpAddress(ipAddress);
        userHistory.setMessage(request.getMessage());
        userHistory.setTimestamp(Timestamp.from(Instant.now()));
        userHistory.setStatus(TransactionStatus.Success.toString());
        userHistory.setDescription(formattedTransactionType);

        try {
            historyRepository.save(userHistory);
            return Error.createResponse("Successfully created history", HttpStatus.OK,
                    "Successfully Create history");
        } catch (Exception e) {
            return Error.createResponse("Failed to process history.", HttpStatus.CONFLICT,
                    "details " + e.getMessage());
        }
    }

    @Override
    public List<HistoryDTO> findByWalletIdAndUserId(Long walletId, Long userId) {
        List<History> historyList = historyRepository.findByWalletIdAndUserId(walletId, userId);

        // Convert to DTOs 
        return historyList.stream()
                .map(history -> new HistoryDTO(history))
                .collect(Collectors.toList());
    }

    @Override
    public List<HistoryDTO> findRecentTransactionsByUserId(Long walletId, Instant timestamp) {
        // Convert Instant to Timestamp
        Timestamp sqlTimestamp = Timestamp.from(timestamp);
        System.out.println("Converted SQL Timestamp: " + sqlTimestamp);

        List<History> historyList = historyRepository.findByTimestampAfterAndWalletId(sqlTimestamp, walletId);

        return historyList.stream()
                .map(HistoryDTO::new)
                .collect(Collectors.toList());
    }

    @Override
    public ResponseEntity<?> countUserHistory(String token,
            Authentication authentication) {
        String username = authentication.getName();
        UserDTO user = userServiceClient.getUserByUsername(username, token);
        if (user == null) {
            return Error.createResponse("User not found", HttpStatus.BAD_REQUEST,
                    "User does not exists in our sysetm.");
        }
        Long countHistory = historyRepository.countByUserId(user.getId());
        return ResponseEntity.ok().body(countHistory);
    }
    

}
