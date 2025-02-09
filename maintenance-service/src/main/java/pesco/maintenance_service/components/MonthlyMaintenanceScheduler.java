package pesco.maintenance_service.components;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pesco.maintenance_service.clients.HistoryServiceClient;
import pesco.maintenance_service.clients.WalletServiceClient;
import pesco.maintenance_service.dtos.WalletDTO;
import pesco.maintenance_service.enums.CurrencyType;
import pesco.maintenance_service.enums.DebtStatus;
import pesco.maintenance_service.enums.TransactionType;
import pesco.maintenance_service.model.DebtCollector;
import pesco.maintenance_service.repository.DebtCollectorRepository;

@Component
public class MonthlyMaintenanceScheduler {


    private static final BigDecimal PLATFORM_FEE_PERCENTAGE = BigDecimal.valueOf(0.0055);
    private final HistoryServiceClient historyServiceClient;
    private final WalletServiceClient walletServiceClient;
    private final DebtCollectorRepository debtCollectorRepository;

    public MonthlyMaintenanceScheduler(HistoryServiceClient historyServiceClient, WalletServiceClient walletServiceClient, DebtCollectorRepository debtCollectorRepository) {
        this.historyServiceClient = historyServiceClient;
        this.walletServiceClient = walletServiceClient;
        this.debtCollectorRepository = debtCollectorRepository;
    }

    @Scheduled(cron = "0 0 0 */20 * ?") // Every 20 days at midnight
    public void calculateAndCreateDebts() {
        List<WalletDTO> usersWallets = walletServiceClient.getAllWallets();

        for (WalletDTO wallet : usersWallets) {
            LocalDate startDate = LocalDate.now().minusDays(28);
            LocalDate endDate = LocalDate.now();

            for (WalletDTO.BalanceDTO balanceDTO : wallet.getBalances()) {
                String currencyType = balanceDTO.getCurrencyCode();
                BigDecimal walletBalance = balanceDTO.getBalance();

                CurrencyType enumCurrencyType = CurrencyType.fromString(currencyType);

                // Fetch total received for the currency type using TransactionService
                BigDecimal totalReceived = historyServiceClient.calculateTotalReceived(
                        wallet.getId(), TransactionType.CREDITED, currencyType, startDate, endDate);

                if (totalReceived == null) {
                    totalReceived = BigDecimal.ZERO;
                }

                // Apply the static platform fee percentage
                BigDecimal maintenanceFee = totalReceived.multiply(PLATFORM_FEE_PERCENTAGE);

                // Check if there's an existing debt for the user
                Optional<DebtCollector> existingDebt = debtCollectorRepository.findByUserIdAndCurrencyTypeAndDebtStatus(
                        wallet.getId(), currencyType, DebtStatus.PENDING);

                if (existingDebt.isPresent()) {
                    DebtCollector debt = existingDebt.get();
                    debt.setDueAmount(debt.getDueAmount().add(maintenanceFee));

                    // Update debt status based on wallet balance
                    if (walletBalance.compareTo(debt.getDueAmount()) < 0) {
                        debt.setDebtStatus(DebtStatus.OVERDUE);
                    } else {
                        debt.setDebtStatus(DebtStatus.PENDING);
                    }

                    debtCollectorRepository.save(debt);
                } else {
                    // Create a new debt if no existing debt is found
                    DebtCollector newDebt = new DebtCollector();
                    newDebt.setUserId(wallet.getId());
                    newDebt.setCurrencyType(enumCurrencyType);
                    newDebt.setAmount(maintenanceFee);
                    newDebt.setDueAmount(BigDecimal.ZERO);
                    newDebt.setDebtStatus(DebtStatus.PENDING);
                    newDebt.setDescription("Monthly maintenance fee");

                    debtCollectorRepository.save(newDebt);
                }
            }
        }
    }
}
