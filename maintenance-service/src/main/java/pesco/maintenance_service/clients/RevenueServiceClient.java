package pesco.maintenance_service.clients;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import pesco.maintenance_service.dtos.RevenueDTO;
import pesco.maintenance_service.enums.CurrencyType;
import pesco.maintenance_service.exceptions.Extraction;
import pesco.maintenance_service.exceptions.UserClientNotFoundException;
import pesco.maintenance_service.payloads.RevenueRequest;
import reactor.core.publisher.Mono;

@Service
public class RevenueServiceClient {

    private final WebClient revenueServiceWebClient;
    private final Extraction extraction;
    
    public RevenueServiceClient(WebClient revenueServiceWebClient, Extraction extraction) {
        this.revenueServiceWebClient = revenueServiceWebClient;
        this.extraction = extraction;
    }

   public RevenueDTO creditWallet(CurrencyType currencyType, BigDecimal amount) {
        try {
            RevenueRequest request = new RevenueRequest(currencyType, amount);

            return this.revenueServiceWebClient.post()
                .uri("/api/v1/revenue/credit")
                .bodyValue(request)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(errorMessage -> {
                                    if (clientResponse.statusCode().is4xxClientError()) {
                                        String details = extraction.extractDetailsFromError(errorMessage);
                                        return Mono.error(new UserClientNotFoundException("Revenue credit failed", details));
                                    }
                                    return Mono.error(new RuntimeException("Server error while crediting revenue wallet"));
                                }))
                .bodyToMono(RevenueDTO.class)
                .block();

        } catch (Exception ex) {
            System.err.println("Error crediting revenue wallet: " + ex.getMessage());
            return null;
        }
    }


   

}
