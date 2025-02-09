package pesco.revenue_service.serviceImplementations;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import pesco.revenue_service.dto.RevenueDTO;
import pesco.revenue_service.enums.CurrencyType;
import pesco.revenue_service.enums.TransactionType;
import pesco.revenue_service.exceptions.RevenueNotFoundException;
import pesco.revenue_service.model.CurrencyBalance;
import pesco.revenue_service.model.Revenue;
import pesco.revenue_service.model.RevenueTransaction;
import pesco.revenue_service.payloads.CreateRevenue;
import pesco.revenue_service.repository.RevenueRepository;
import pesco.revenue_service.repository.RevenueTransactionRepository;
import pesco.revenue_service.services.RevenueService;

@Service
public class RevenueServiceImplementations implements RevenueService {

    private final RevenueRepository revenueRepository;
    private final RevenueTransactionRepository transactionRepository;

    @Autowired
    public RevenueServiceImplementations(RevenueRepository revenueRepository, RevenueTransactionRepository transactionRepository) {
        this.revenueRepository = revenueRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public RevenueDTO getRevenueByUser(Long userId) {
        return revenueRepository.findFirstByOrderByIdAsc()
                .map(RevenueDTO::new)
                .orElseThrow(() -> new RevenueNotFoundException("Revenue data not found"));
    }

    @Override
    public ResponseEntity<?> creditRevenue(CreateRevenue request) {
        // Check if a revenue record exists; if not, create and save a new one
        Revenue get_revenue = revenueRepository.findFirstByOrderByIdAsc().orElse(null);

        if (get_revenue != null) {
            // Revenue exists, update the specific wallet (USD, AUD, EUR, etc.)
            updateCurrencyBalance(get_revenue, request.getType(), request.getAmount());

            // Save updates to revenue and log the transaction
            revenueRepository.save(get_revenue);
            logTransaction(get_revenue, request.getType(), request.getAmount(), TransactionType.CREDITED);

            return ResponseEntity.ok(get_revenue);
        }

        // Else found null, create new revenue wallet for all currencies
        Revenue revenueWallet = new Revenue();
        revenueWallet.setBalances(new ArrayList<>());
        initializeAllCurrencyRevenueWallets(revenueWallet);

        // Update the specific currency wallet being credited
        updateCurrencyBalance(revenueWallet, request.getType(), request.getAmount());

        // Save the newly created revenue wallet
        revenueRepository.save(revenueWallet);
        logTransaction(revenueWallet, request.getType(), request.getAmount(), TransactionType.CREDITED);

        return ResponseEntity.ok(revenueWallet);
    }

    /**
     * Updates the balance of a specific currency in the revenue wallet.
     */
    private void updateCurrencyBalance(Revenue revenue, CurrencyType currencyType, BigDecimal amount) {
        for (CurrencyBalance balance : revenue.getBalances()) {
            if (balance.getCurrencyCode().equalsIgnoreCase(currencyType.name())) {
                balance.setBalance(balance.getBalance().add(amount));
                return;
            }
        }
        // If currency does not exist in balances, add it
        revenue.getBalances().add(new CurrencyBalance(currencyType.name(), getCurrencySymbol(currencyType), amount));
    }

    /**
     * Logs a revenue transaction.
     */
    private void logTransaction(Revenue revenue, CurrencyType currencyType, BigDecimal amount, TransactionType type) {
        RevenueTransaction transaction = new RevenueTransaction();
        transaction.setRevenue(revenue);
        transaction.setTransactionType(type);
        transaction.setAmount(amount);
        transaction.setWallet(currencyType);
        transactionRepository.save(transaction);
    }

    /**
     * Initializes all currency revenue wallets with zero balance.
     */
    private void initializeAllCurrencyRevenueWallets(Revenue wallet) {
        List<CurrencyBalance> balances = new ArrayList<>();
        for (CurrencyType currency : CurrencyType.values()) {
            balances.add(new CurrencyBalance(
                    currency.name(),
                    getCurrencySymbol(currency),
                    BigDecimal.ZERO));
        }
        wallet.setBalances(balances);
    }

    /**
     * Returns the currency symbol for a given currency type.
     */
    private String getCurrencySymbol(CurrencyType currency) {
        switch (currency) {
            case USD:
                return "$";
            case EUR:
                return "€";
            case NGN:
                return "₦";
            case GBP:
                return "£";
            case JPY:
                return "¥";
            case AUD:
                return "A$";
            case CAD:
                return "C$";
            case CHF:
                return "CHF";
            case CNY:
                return "¥";
            case INR:
                return "₹";
            default:
                throw new IllegalArgumentException("Unknown currency type: " + currency);
        }
    }

}
