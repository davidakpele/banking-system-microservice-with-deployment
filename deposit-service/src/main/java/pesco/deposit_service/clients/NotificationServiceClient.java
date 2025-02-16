package pesco.deposit_service.clients;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import pesco.deposit_service.enums.CurrencyType;

@Service
public class NotificationServiceClient {

    private final WebClient notificationServiceWebClient;

    @Autowired
    public NotificationServiceClient(@Qualifier("notificationServiceWebClient") WebClient notificationServiceWebClient) {
        this.notificationServiceWebClient = notificationServiceWebClient;
    }

    public Object sendDepositNotification(String email, String username, BigDecimal amount, CurrencyType currencyType, BigDecimal totalBalance) {
          try {
            // Create the request body 
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("recipientEmail", email);
            requestBody.put("recipientName", username);
            requestBody.put("depositAmount", amount);
            requestBody.put("transactionTime", LocalDateTime.now());
            requestBody.put("totalBalance", totalBalance);

            // Send the POST request
            return notificationServiceWebClient.post()
                .uri("/send/deposit-wallet-message") 
                .bodyValue(requestBody) 
                .retrieve()
                .bodyToMono(Object.class) 
                .block(); 
        } catch (Exception ex) {
            // Handle exceptions
            System.err.println("Error sending transaction notifications: " + ex.getMessage());
            return null; 
        }
    }

}
