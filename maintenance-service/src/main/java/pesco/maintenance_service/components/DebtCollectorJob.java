package pesco.maintenance_service.components;

import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import pesco.maintenance_service.clients.NotificationServiceClient;
import pesco.maintenance_service.clients.RevenueServiceClient;
import pesco.maintenance_service.clients.UserServiceClient;
import pesco.maintenance_service.clients.WalletServiceClient;
import pesco.maintenance_service.dtos.RevenueDTO;
import pesco.maintenance_service.dtos.UserDTO;
import pesco.maintenance_service.dtos.WalletDTO;
import pesco.maintenance_service.enums.DebtStatus;
import pesco.maintenance_service.model.DebtCollector;
import pesco.maintenance_service.repository.DebtCollectorRepository;

@Component
@RequiredArgsConstructor
public class DebtCollectorJob {

    private final DebtCollectorRepository debtCollectorRepository;
    private final WalletServiceClient walletServiceClient;
    private final NotificationServiceClient notificationServiceClient;
    private final RevenueServiceClient revenueServiceClient;
    private final UserServiceClient userServiceClient;

    @Scheduled(fixedRate = 5000)
    public void processDebts() {
        List<DebtCollector> pendingDebts = debtCollectorRepository.findByDebtStatus(DebtStatus.PENDING);

        for (DebtCollector debt : pendingDebts) {
            WalletDTO wallet = walletServiceClient.getWalletById(debt.getUserId());
            List<UserDTO> users = userServiceClient.findAllUserId(debt.getUserId());

            if (users != null) {
                if (wallet == null)
                    continue;

                // Extract first and last name
                UserDTO user = users.get(0);
                String firstName = user.getRecords().get(0).getFirstName();
                String usersEmail = user.getEmail();

                /* Extract the specific user wallet */
                wallet.getBalances().stream()
                    .filter(balance -> balance.getCurrencyCode().equals(debt.getCurrencyType().toString()))
                    .findFirst()
                    .ifPresent(currencyBalance -> {
                        if (currencyBalance.getBalance().compareTo(debt.getAmount()) >= 0) {
                            // Deduct the fee from user's wallet
                            currencyBalance.setBalance(currencyBalance.getBalance().subtract(debt.getAmount()));

                            walletServiceClient.updateWalletBalance(debt.getUserId(), debt.getCurrencyType(),
                                    currencyBalance.getBalance());

                            // Credit revenue wallet
                            RevenueDTO revenue = revenueServiceClient.creditWallet(debt.getCurrencyType(),
                                    debt.getAmount());

                            if (revenue != null) {
                                // Delete the debt record since it has been paid
                                debtCollectorRepository.delete(debt);
                                String content = "Hello, "+firstName+",\n\n This is a receipt for your electronic monthly maintenance bill on your wallet. \n\nThanks for banking with us.!";
                                // Notify user with first and last name
                                notificationServiceClient.sendNotification(firstName,
                                        usersEmail, debt.getAmount(), currencyBalance.getBalance(), content, debt.getCurrencyType().toString());
                            }
                        } else {
                            // Update debt status to OVERDUE
                            debt.setDebtStatus(DebtStatus.OVERDUE);
                            debtCollectorRepository.save(debt);
                        }
                    });
            }
        }
    }
}
