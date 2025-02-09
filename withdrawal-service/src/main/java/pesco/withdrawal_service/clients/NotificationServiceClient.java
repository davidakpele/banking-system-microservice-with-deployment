package pesco.withdrawal_service.clients;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class NotificationServiceClient {
 
    private final WebClient notificationServiceWebClient;

    @Autowired
    public NotificationServiceClient(WebClient notificationServiceWebClient) {
        this.notificationServiceWebClient = notificationServiceWebClient;
    }

    public Object sendDebitAlert(String senderEmail, String senderUsername, 
                                           String recipientUsername, 
                                           BigDecimal amount, String currency, 
                                           BigDecimal feeAmount, BigDecimal senderNewWalletBalance) {
        try {
            // Create the request body 
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("senderEmail", senderEmail);
            requestBody.put("senderFullName", senderUsername);
            requestBody.put("receiverFullName", recipientUsername);
            requestBody.put("transferAmount", amount);
            requestBody.put("currency", currency);
            requestBody.put("feeAmount", feeAmount);
            requestBody.put("balance", senderNewWalletBalance);

            // Send the POST request
            return notificationServiceWebClient.post()
                    .uri("/send/debit-wallet-message") 
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

    public Object sendCreditAlert(String username, String email, String recipientUsername, BigDecimal amount, String currency, BigDecimal recieverNewWalletBalance) {
        try {
            // Create the request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("recipientEmail", email);
            requestBody.put("senderFullName", username);
            requestBody.put("receiverFullName", recipientUsername);
            requestBody.put("transferAmount", amount);
            requestBody.put("currency", currency);
            requestBody.put("recipientTotalBalance", recieverNewWalletBalance);

            // Send the POST request
            return notificationServiceWebClient.post()
                    .uri("/send/credit-wallet-message")
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
