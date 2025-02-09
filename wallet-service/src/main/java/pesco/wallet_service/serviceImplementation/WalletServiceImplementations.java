package pesco.wallet_service.serviceImplementation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import pesco.wallet_service.clients.HistoryClient;
import pesco.wallet_service.clients.UserServiceClient;
import pesco.wallet_service.dtos.UserDTO;
import pesco.wallet_service.enums.CurrencyType;
import pesco.wallet_service.models.CurrencyBalance;
import pesco.wallet_service.models.Wallet;
import pesco.wallet_service.payloads.CreateWalletRequest;
import pesco.wallet_service.payloads.DeductAmountRequest;
import pesco.wallet_service.payloads.TransactionRequest;
import pesco.wallet_service.payloads.UpdateWalletRequest;
import pesco.wallet_service.respositories.WalletRepository;
import pesco.wallet_service.services.WalletService;
import pesco.wallet_service.exceptions.Error;
import pesco.wallet_service.exceptions.UserClientNotFoundException;

@Service
@RequiredArgsConstructor
public class WalletServiceImplementations implements WalletService {

    private final WalletRepository walletRepository;
    private final UserServiceClient userServiceClient;
    private final HistoryClient historyClient;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Optional<Wallet> GetWalletByUserId(Long userId, String token) {
        // Fetch UserDTO if needed
        UserDTO user = userServiceClient.getUserById(userId, token);
        return walletRepository.findByUserId(user.getId());
    }

    @Override
    public ResponseEntity<?> CreateUserTransferPin(TransactionRequest request, String token,
            Authentication authentication) {
        String requestUsername = authentication.getName();
        UserDTO user = userServiceClient.getUserByUsername(requestUsername, token);

        Optional<Wallet> wallet = walletRepository.findByUserId(user.getId());

        if (!user.getUsername().equals(requestUsername)) {
            return Error.createResponse(
                    "Fraudulent action is taken here, You are not the authorized user to operate this wallet.",
                    HttpStatus.FORBIDDEN,
                    "One more attempt from you again, you will be reported to the Economic and Financial Crimes Commission (EFCC).");
        }
        if (request.getTransferpin().isEmpty()) {
            return Error.createResponse("Your withdrawal/transfer pin is required*", HttpStatus.NOT_FOUND,
                    "Please provide your withdrawal/transfer pin and please don't share it with anyone for security reasons.");
        }
        String providedPin = request.getTransferpin();

        // Check if the pin is not exactly 4 digits or contains non-digit characters
        if (providedPin.length() != 4 || !providedPin.matches("\\d{4}")) {
            return Error.createResponse(
                    "Invalid input. Please provide exactly 4 digits.",
                    HttpStatus.BAD_REQUEST,
                    "Your pin must be exactly 4 numeric digits.");
        }

        if (wallet.isPresent()) {
            Wallet extractWallet = wallet.get();

            // Set all balances to 0.00
            extractWallet.getBalances().forEach(balance -> 
                balance.setBalance(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
            );

            // Set userId and encoded transfer pin
            extractWallet.setUserId(user.getId());
            extractWallet.setPassword(passwordEncoder.encode(providedPin));

            // Save updated wallet
            walletRepository.save(extractWallet);
        }

        // Update the user's transfer pin in their user record
        if (userServiceClient.UpdateUserTranferPinInUserRecord(token, user.getId(), true)) {
            return Error.createResponse(
                    "Withdrawal/transfer password successfully set, you can now make withdrawals or transfers.",
                    HttpStatus.OK, "Success");
        }

        return Error.createResponse("Sorry, this user does not exist in our system.", HttpStatus.BAD_REQUEST,
                "User does not exist, please provide a valid username.");
    }

    @Override
    public ResponseEntity<?> GetBalance(String username, String token) {
        // Fetch user details
        UserDTO user = userServiceClient.getUserByUsername(username, token);

        // Fetch main wallet balance
        Optional<Wallet> walletOptional = walletRepository.findByUserId(user.getId());

        // Build the response
        Map<String, Object> response = new HashMap<>();

        if (walletOptional.isPresent()) {
            Wallet wallet = walletOptional.get();
            // Prepare list of wallet balances with currency details
            List<Map<String, Object>> balanceDetails = wallet.getBalances().stream()
                    .map(currencyBalance -> {
                        Map<String, Object> balanceInfo = new HashMap<>();
                        
                        balanceInfo.put("currency_code", currencyBalance.getCurrencyCode());
                        balanceInfo.put("symbol", currencyBalance.getCurrencySymbol());
                        balanceInfo.put("balance", 
                                FormatBigDecimal(currencyBalance.getBalance()));
                        return balanceInfo;
                    })
                    .collect(Collectors.toList());

            // Calculate central balance (total of all currency balances)
            // BigDecimal centralBalance = wallet.getBalances().stream()
            //     .map(CurrencyBalance::getBalance)
            //     .reduce(BigDecimal.ZERO, BigDecimal::add)
            //     .setScale(2, RoundingMode.HALF_UP);

            // String formattedBalance = FormatBigDecimal(centralBalance);

            // Fetch transaction history count
            Long transactionCount = historyClient.getTransactionCount(user.getId(), token);

            String transactionHistoryLabel = transactionCount > 1 ? transactionCount + " times" : transactionCount ==0 ? "No history" : "once";
            
            //response.put("central_balance", formattedBalance);
            response.put("transaction_history_count", transactionHistoryLabel);
            response.put("wallet_balances", balanceDetails);
            response.put("user_authentication_details", user.getRecords().get(0).isTransferPin());
                    
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
    
        response.put("message", "Wallet not found");
        response.put("details", "User wallet not found.");

        return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
    }

    @SuppressWarnings("deprecation")
     @Override
    @Cacheable(value = "walletBalances", key = "#userId + '-' + #currency.name()")
    public ResponseEntity<?> GetBalanceByCurrencyType(Long userId, CurrencyType currency, String token) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Fetch user details
            UserDTO user = userServiceClient.getUserById(userId, token);
            if (user == null) {
                response.put("error", "User not found");
                response.put("details", "User not found: No user data returned for ID " + userId);
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

            // Fetch wallet with specific currency code
            Optional<Wallet> walletOpt = walletRepository.findWalletByUserIdAndCurrencyCode(userId, currency.name());

            if (walletOpt.isEmpty()) {
                response.put("error", "Wallet or currency not found for user ID.");
                response.put("details", "Wallet or currency not found for user ID " + userId + " and currency " + currency);
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

            Wallet wallet = walletOpt.get();

            // Find the specific currency balance
            CurrencyBalance currencyBalance = wallet.getBalances()
                    .stream()
                    .filter(balance -> balance.getCurrencyCode().equals(currency.name()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Currency not found in wallet"));

            BigDecimal balance = currencyBalance.getBalance().setScale(2, BigDecimal.ROUND_HALF_UP);

            // Format balance
            String formattedBalance = FormatBigDecimal(balance);

            // Build response
            response.put("id", userId);
            response.put("currency", currency);
            response.put("formatted_balance", formattedBalance);
            response.put("symbol", currencyBalance.getCurrencySymbol());

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (UserClientNotFoundException ex) {
            // Handle user not found error
            response.put("error", "User not found");
            response.put("details", ex.getMessage());
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        } catch (RuntimeException ex) {
            // Handle general server errors
            response.put("error", "Server error");
            response.put("details", ex.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public String FormatBigDecimal(BigDecimal value) {
        String pattern = "#,##0.00";
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator(',');
        symbols.setDecimalSeparator('.');
        DecimalFormat decimalFormat = new DecimalFormat(pattern, symbols);
        return decimalFormat.format(value);
    }

    @Override
    public ResponseEntity<?> createWallet(CreateWalletRequest request) {
        try {
            // Create and initialize the wallet
            Wallet wallet = new Wallet();
            wallet.setUserId(request.getUserId());
            wallet.setBalances(new ArrayList<>());
            initializeAllCurrencyWallets(wallet); 

            // Save the wallet
            walletRepository.save(wallet);

            // Return success response
            return ResponseEntity.ok("Wallet Successfully Created!");
        } catch (Exception e) {
            // Handle exceptions and return an error response
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to create wallet: " + e.getMessage());
        }
    }

    private void initializeAllCurrencyWallets(Wallet wallet) {
        List<CurrencyBalance> balances = new ArrayList<>();
        for (CurrencyType currency : CurrencyType.values()) {
            balances.add(new CurrencyBalance(
                    currency.name(), 
                    getCurrencySymbol(currency), 
                    BigDecimal.ZERO 
            ));
        }
        wallet.setBalances(balances);
    }

    private String getCurrencySymbol(CurrencyType currency) {
        switch (currency) {
            case USD: return "$";
            case EUR: return "€";
            case NGN: return "₦";
            case GBP: return "£";
            case JPY: return "¥";
            case AUD: return "A$";
            case CAD: return "C$";
            case CHF: return "CHF";
            case CNY: return "¥";
            case INR: return "₹";
            default: throw new IllegalArgumentException("Unknown currency type: " + currency);
        }
    }

    @CacheEvict(value = "walletBalances", key = "#userId + '-' + #currency.name()")
    public void evictBalanceCache(Long userId, CurrencyType currency) {
        // This method will be called to clear the cache when the balance is updated.
    }

    @Override
    public ResponseEntity<Optional<Wallet>> findByUserId(Long id) {
        Optional<Wallet> wallet = walletRepository.findByUserId(id);
        return ResponseEntity.ok(wallet);
    }

    @Override
    public ResponseEntity<?> updateUserWallet(UpdateWalletRequest request) {
        // Find the wallet by userId
        Optional<Wallet> optionalWallet = walletRepository.findByUserId(request.getUserId());

        if (optionalWallet.isPresent()) {
            Wallet wallet = optionalWallet.get();

            // Check if the currency balance for the specific currency type exists
            List<CurrencyBalance> balances = wallet.getBalances();
            if (balances == null) {
                balances = new ArrayList<>();
            }
 
            boolean currencyFound = false;

            for (CurrencyBalance currencyBalance : balances) {
                // Compare currency codes case-insensitively
                if (currencyBalance.getCurrencyCode().equalsIgnoreCase(request.getCurrencyType().toString())) {
                    // If found, update the balance
                    currencyBalance.setBalance(currencyBalance.getBalance().add(request.getAmount()));
                    currencyFound = true;
                    break;
                }
            }

            // If the currency doesn't exist in the wallet, add a new balance for the currency
            if (!currencyFound) {
                CurrencyBalance newBalance = new CurrencyBalance();
                newBalance.setCurrencyCode(request.getCurrencyType().toString());
                newBalance.setCurrencySymbol(request.getCurrencyType().toString());
                newBalance.setBalance(request.getAmount());
                balances.add(newBalance);
            }

            // Save the updated wallet
            wallet.setBalances(balances);
            walletRepository.save(wallet);

            // Return a successful response with the updated wallet
            return ResponseEntity.ok(wallet);
        }

        // If wallet is not found, return a 404 response with error details
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Wallet not found for userId " + request.getUserId());
    }

    @SuppressWarnings("deprecation")
    @Override
    public ResponseEntity<?> deductUserWallet(DeductAmountRequest request) {
        Optional<Wallet> optionalWallet = walletRepository.findByUserId(request.getId());

        if (optionalWallet.isPresent()) {
            Wallet wallet = optionalWallet.get();

            // Find the currency balance for the specified currency type
            List<CurrencyBalance> balances = wallet.getBalances();
            CurrencyBalance currencyBalance = null;
            for (CurrencyBalance balance : balances) {
                if (balance.getCurrencyCode().equals(request.getCurrencyType().name())) {
                    currencyBalance = balance;
                    break;
                }
            }

            // If the currency balance is not found
            if (currencyBalance == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Currency " + request.getCurrencyType() + " not found in wallet.");
            }

            // Check if the wallet has sufficient balance for the deduction
            if (currencyBalance.getBalance().compareTo(request.getAmount()) < 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Insufficient balance to deduct the requested amount.");
            }

            // Deduct the amount from the user's wallet
            currencyBalance.setBalance(currencyBalance.getBalance().subtract(request.getAmount()));

            // Save the updated wallet
            wallet.setBalances(balances);
            walletRepository.save(wallet);

            // Return the updated wallet balance and success message
            return ResponseEntity.ok("Amount deducted successfully. New balance: "
                    + currencyBalance.getBalance().setScale(2, BigDecimal.ROUND_HALF_UP));
        }

        // If wallet is not found, return a 404 response with error details
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Wallet not found for userId " + request.getId());
    }

    @Override
    public ResponseEntity<?> findById(Long walletId) {
        Optional<Wallet> wallet = walletRepository.findById(walletId);
        return ResponseEntity.ok(wallet);
    }

    
}
