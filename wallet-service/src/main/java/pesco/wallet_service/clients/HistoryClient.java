package pesco.wallet_service.clients;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import pesco.wallet_service.exceptions.UserClientNotFoundException;
import reactor.core.publisher.Mono;
import org.springframework.beans.factory.annotation.Value;


@Service
public class HistoryClient {

    private final WebClient webClient;

    @Autowired
    public HistoryClient(WebClient.Builder webClientBuilder, @Value("${history-service.base-url}") String baseUrl) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
    }

    public Long getTransactionCount(Long userId, String token) {
        // Call the endpoint and return the transaction count
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                .path("/api/v1/history/transactions/count")
                .queryParam("userId", userId)
                .build())
                .headers(headers -> headers.setBearerAuth(token))
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(errorMessage -> {
                                    if (clientResponse.statusCode().is4xxClientError()) {
                                        String details = ExtractDetailsFromError(errorMessage);
                                        return Mono.error(
                                                new UserClientNotFoundException("User not found: " + details,
                                                        errorMessage));
                                    }
                                    return Mono.error(new RuntimeException("Server error: " + errorMessage));
                                }))
                .bodyToMono(Long.class)
                .block();
    }

    private String ExtractDetailsFromError(String errorMessage) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(errorMessage);
            return rootNode.path("message").asText();
        } catch (JsonProcessingException e) {
            return "No details available";
        }
    }
}
