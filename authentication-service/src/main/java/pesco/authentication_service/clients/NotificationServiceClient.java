package pesco.authentication_service.clients;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class NotificationServiceClient {
    
    private final WebClient notificationServiceWebClient;

    @Autowired
    public NotificationServiceClient(WebClient notificationServiceWebClient) {
        this.notificationServiceWebClient = notificationServiceWebClient;
    }

    public Object sendVerificationEmail(String email, String content, String verificationLink, String username) {
       try {
            // Create the request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("email", email);
            requestBody.put("username", username);
            requestBody.put("link", verificationLink);
            requestBody.put("message", content);
            // Send the POST request
            return notificationServiceWebClient.post()
                .uri("/send/verification-message") 
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

    public Object sendOptEmail(String email, String otp, String restPassword, String configTwoFactorAuth,
            String configTwoFactorAuthRecovery) {
        try {
            // Create the request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("email", email);
            requestBody.put("otp", otp);
            requestBody.put("restPassword", restPassword);
            requestBody.put("configTwoFactorAuth", configTwoFactorAuth);
            requestBody.put("configTwoFactorAuthRecovery", configTwoFactorAuthRecovery);

            // Send the POST request
            return notificationServiceWebClient.post()
                    .uri("/send/otp-message")
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

    public Object sendPasswordResetMessage(String email, String username, String content, String url,
            String customerEmail) {
        try {
            // Create the request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("email", email);
            requestBody.put("username", username);
            requestBody.put("message", content);
            requestBody.put("url", url);
            requestBody.put("customerEmail", customerEmail);

            // Send the POST request
            return notificationServiceWebClient.post()
                    .uri("/send/password-reset-message")
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
