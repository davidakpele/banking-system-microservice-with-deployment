package pesco.maintenance_service.clients;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import pesco.maintenance_service.enums.TransactionType;
import pesco.maintenance_service.exceptions.Extraction;
import pesco.maintenance_service.exceptions.UserClientNotFoundException;
import reactor.core.publisher.Mono;

@Service
public class HistoryServiceClient {

    private final WebClient historyServiceWebClient;
    private final Extraction extraction;
    
    public HistoryServiceClient(WebClient historyServiceWebClient, Extraction extraction) {
        this.historyServiceWebClient = historyServiceWebClient;
        this.extraction = extraction;
    }
   
    public BigDecimal calculateTotalReceived(Long id, TransactionType credited, String currencyType,
            LocalDate startDate, LocalDate endDate) {
       try {
            return this.historyServiceWebClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/history/total-received")
                    .queryParam("userId", id)
                    .queryParam("transactionType", credited)
                    .queryParam("currencyType", currencyType)
                    .queryParam("startDate", startDate)
                    .queryParam("endDate", endDate)
                    .build())
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(errorMessage -> {
                                    if (clientResponse.statusCode().is4xxClientError()) {
                                        String details = extraction.extractDetailsFromError(errorMessage);
                                        return Mono.error(new UserClientNotFoundException("Transaction data not found", details));
                                    }
                                    return Mono.error(new RuntimeException("Server error"));
                                }))
                .bodyToMono(BigDecimal.class)
                .block();
        } catch (Exception ex) {
            System.err.println("Error fetching total received amount: " + ex.getMessage());
            return BigDecimal.ZERO; // Return 0 if an error occurs
        }
    
    }

    
}
