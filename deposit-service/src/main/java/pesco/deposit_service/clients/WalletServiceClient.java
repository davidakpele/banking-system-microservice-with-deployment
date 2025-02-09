package pesco.deposit_service.clients;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import pesco.deposit_service.dto.WalletDTO;
import pesco.deposit_service.enums.CurrencyType;
import pesco.deposit_service.exceptions.UserClientNotFoundException;
import pesco.deposit_service.payloads.CreditWalletRequest;
import reactor.core.publisher.Mono;

@Service
public class WalletServiceClient {

    private final WebClient walletServiceWebClient;

    @Autowired
    public WalletServiceClient(WebClient walletServiceWebClient) {
        this.walletServiceWebClient = walletServiceWebClient;
    }

    public WalletDTO findByUserId(Long userId, String token) {
        return this.walletServiceWebClient.get()
            .uri("/api/v1/wallet/userId/{userId}", userId)
            .header("Authorization", "Bearer " + token)
            .retrieve()
            .bodyToMono(WalletDTO.class)
            .block(); 
    }

    public BigDecimal FetchUserBalance(Long userId, String currencyCode, String token) {
        String url = String.format("/api/v1/wallet/balance/userId/%d/currency/%s", userId, currencyCode);
        Map<String, Object> response = walletServiceWebClient.get()
                .uri(url)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();

        // Extract and return the balance
        if (response != null && response.containsKey("formatted_balance")) {
            try {
                String formattedBalance = response.get("formatted_balance").toString();
                return parseFormattedString(formattedBalance);
            } catch (ParseException e) {
                throw new RuntimeException("Failed to parse balance: " + e.getMessage(), e);
            }
        }

        throw new RuntimeException("Failed to fetch balance for userId: " + userId + " and currencyCode: " + currencyCode);
    }

    public WalletDTO creditUserWallet(BigDecimal amount, CurrencyType currencyType, Long userId, String token) {
        CreditWalletRequest creditWalletRequest = new CreditWalletRequest(amount, currencyType, userId);

        return this.walletServiceWebClient.put()
                .uri("/api/v1/wallet/update")
                .header("Authorization", "Bearer " + token)
                .bodyValue(creditWalletRequest) 
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(errorMessage -> {
                                    if (clientResponse.statusCode().is4xxClientError()) {
                                        String details = extractDetailsFromError(errorMessage);
                                        return Mono.error(new UserClientNotFoundException(
                                                "Client error while crediting wallet", details));
                                    }
                                    return Mono.error(new RuntimeException("Server error while crediting wallet"));
                                }))
                .bodyToMono(WalletDTO.class)
                .block();
    }
    
    private String extractDetailsFromError(String errorMessage) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(errorMessage);
            return rootNode.path("message").asText();
        } catch (JsonProcessingException e) {
            return "No details available";
        }
    }

    public static BigDecimal parseFormattedString(String formattedValue) throws ParseException {
        String pattern = "#,##0.00";
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator(',');
        symbols.setDecimalSeparator('.');
        DecimalFormat decimalFormat = new DecimalFormat(pattern, symbols);
        
        Number number = decimalFormat.parse(formattedValue);
        return new BigDecimal(number.toString());
    }


}
