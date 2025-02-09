package pesco.maintenance_service.clients;

import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import pesco.maintenance_service.exceptions.Extraction;
import pesco.maintenance_service.exceptions.UserClientNotFoundException;
import pesco.maintenance_service.payloads.NotificationRequest;
import reactor.core.publisher.Mono;

@Service
public class NotificationServiceClient {

    private final WebClient notificationServiceWebClient;
    private final Extraction extraction;

    public NotificationServiceClient(WebClient notificationServiceWebClient, Extraction extraction) {
        this.notificationServiceWebClient = notificationServiceWebClient;
        this.extraction = extraction;
    }

    public void sendNotification(String firstName, String email,
            BigDecimal amount, BigDecimal balance, String content, String currencyType) {
        try {
            NotificationRequest request = new NotificationRequest(firstName, email, amount, balance, content,
                    currencyType);

            this.notificationServiceWebClient.post()
                    .uri("/send/matainance-wallet-message")
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .flatMap(errorMessage -> {
                                        if (clientResponse.statusCode().is4xxClientError()) {
                                            String details = extraction.extractDetailsFromError(errorMessage);
                                            return Mono.error(
                                                    new UserClientNotFoundException("Notification failed", details));
                                        }
                                        return Mono
                                                .error(new RuntimeException("Server error while sending notification"));
                                    }))
                    .toBodilessEntity()
                    .block();

        } catch (Exception ex) {
            System.err.println("Error sending notification: " + ex.getMessage());
        }
    }

}
