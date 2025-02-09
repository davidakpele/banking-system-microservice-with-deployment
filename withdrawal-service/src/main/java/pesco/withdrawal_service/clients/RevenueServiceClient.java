package pesco.withdrawal_service.clients;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import pesco.withdrawal_service.enums.CurrencyType;

@Service
public class RevenueServiceClient {

    private final WebClient revenueServiceWebClient;

    @Autowired
    public RevenueServiceClient(WebClient revenueServiceWebClient) {
        this.revenueServiceWebClient = revenueServiceWebClient;
    }

    public Object addToPlatformRevenue(BigDecimal feeAmount, CurrencyType currencyType) {
        try {
            // Create the request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("amount", feeAmount);
            requestBody.put("type", currencyType);

            // Send the POST request
            return revenueServiceWebClient.post()
                    .uri("/add/revenue") 
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Object.class) 
                    .block();
        } catch (Exception ex) {
            // Handle exceptions
            System.err.println("Error adding to platform revenue: " + ex.getMessage());
            return null; 
        }
    }

}
