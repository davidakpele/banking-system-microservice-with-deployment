package pesco.maintenance_service.clients;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import pesco.maintenance_service.dtos.WalletDTO;
import pesco.maintenance_service.enums.CurrencyType;
import pesco.maintenance_service.exceptions.Extraction;
import pesco.maintenance_service.exceptions.UserClientNotFoundException;
import pesco.maintenance_service.payloads.UpdateWalletRequest;
import reactor.core.publisher.Mono;

@Service
public class WalletServiceClient {

    private final WebClient  walletServiceWebClient;
    private final Extraction extraction;

    public WalletServiceClient(WebClient walletServiceWebClient, Extraction extraction) {
        this.walletServiceWebClient = walletServiceWebClient;
        this.extraction = extraction;
    }
 
    public WalletDTO getWalletById(Long userId) {
        WalletDTO walletDTO = this.walletServiceWebClient.get()
                .uri("/wallet/userId/{userId}", userId)
                .retrieve()
                .bodyToMono(WalletDTO.class)
                .block();
        return walletDTO;
    }

    public void updateWalletBalance(Long userId, CurrencyType currencyType, BigDecimal balance) {
        UpdateWalletRequest request = new UpdateWalletRequest(userId, balance, currencyType);
        this.walletServiceWebClient.put()
                .uri("/wallet/update")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(WalletDTO.class)
                .block();
    }

    public List<WalletDTO> getAllWallets() {
        try {
            return this.walletServiceWebClient.get()
                .uri("/api/v1/wallets")
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(errorMessage -> {
                                    if (clientResponse.statusCode().is4xxClientError()) {
                                        String details = extraction.extractDetailsFromError(errorMessage);
                                        return Mono.error(new UserClientNotFoundException("Wallets not found", details));
                                    }
                                    return Mono.error(new RuntimeException("Server error while fetching wallets"));
                                }))
                .bodyToFlux(WalletDTO.class) // Expecting a list response
                .collectList()
                .block();

        } catch (Exception ex) {
            System.err.println("Error fetching wallets: " + ex.getMessage());
            return Collections.emptyList(); 
        }
    }
}
