package pesco.notification_service.messageProducer;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import pesco.notification_service.configurations.RabbitMQConfig;
import pesco.notification_service.payloads.CreditWalletNotification;
import pesco.notification_service.payloads.DebitWalletNotification;
import pesco.notification_service.payloads.DepositWalletNotification;
import pesco.notification_service.payloads.MaintenanceDeductionNotification;

@Service
@RequiredArgsConstructor
public class WalletMessageProducer {

    private final RabbitTemplate rabbitTemplate;

    /**
     * Sends a notification for crediting a wallet.
     * 
     * @param recipientEmail
     * @param transferAmount
     * @param senderFullName
     * @param receiverFullName
     * @param RecipientTotalBalance
     */
    public void sendCreditWalletNotification(String recipientEmail, BigDecimal transferAmount, String senderFullName, String receiverFullName, BigDecimal RecipientTotalBalance, String currency) {
        CreditWalletNotification creditWalletNotification = new CreditWalletNotification(recipientEmail, transferAmount, senderFullName, receiverFullName, RecipientTotalBalance, currency);
        rabbitTemplate.convertAndSend(RabbitMQConfig.WALLET_EXCHANGE, RabbitMQConfig.ROUTING_KEY_CREDIT_WALLET, creditWalletNotification);
    }

    /**
     * Sends a notification for debiting a wallet.
     * 
     * @param senderEmail
     * @param feeAmount
     * @param transferAmount
     * @param senderFullName
     * @param receiverFullName
     * @param balance
     */
    public void sendDebitWalletNotification(String senderEmail, BigDecimal feeAmount, BigDecimal transferAmount, String senderFullName, String receiverFullName, BigDecimal balance, String currency) {
        DebitWalletNotification debitWalletNotification = new DebitWalletNotification(senderEmail, feeAmount, transferAmount, senderFullName, receiverFullName, balance, currency);
        rabbitTemplate.convertAndSend(RabbitMQConfig.WALLET_EXCHANGE, RabbitMQConfig.ROUTING_KEY_DEBIT_WALLET, debitWalletNotification);
    }

    /**
     * Sends a notification for depositing into a wallet.
     * 
     * @param recipientEmail
     * @param recipientName
     * @param depositAmount
     * @param transactionTime
     * @param totalBalance
     */
    public void sendDepositWalletNotification(String recipientEmail, String recipientName, BigDecimal depositAmount, LocalDateTime transactionTime, BigDecimal totalBalance) {
        DepositWalletNotification depositWalletNotification = new DepositWalletNotification(recipientEmail, recipientName, depositAmount, transactionTime, totalBalance);
        rabbitTemplate.convertAndSend(RabbitMQConfig.WALLET_EXCHANGE, RabbitMQConfig.ROUTING_KEY_DEPOSIT_WALLET, depositWalletNotification);
    }

    /**
     * Sends a notification for maintainance to users.
     * 
     * @param email
     * @param firstName
     * @param amount
     * @param balance
     * @param content
     */
    public void sendMaintenanceNotification(String email, String firstName, BigDecimal amount, BigDecimal balance, String content) {
        MaintenanceDeductionNotification maintenanceDeductionNotification = new MaintenanceDeductionNotification(email, firstName, amount, balance, content);
        rabbitTemplate.convertAndSend(RabbitMQConfig.WALLET_EXCHANGE, RabbitMQConfig.ROUTING_KEY_WALLET_MAINTENANCE_SERVICE_DEDUCTION, maintenanceDeductionNotification);
    }
}
