package pesco.authentication_service.clients;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.reactive.function.client.WebClient;
import pesco.authentication_service.payloads.WalletRequest;
import org.springframework.stereotype.Service;

@Service
public class WalletServiceClient {

    private final WebClient walletServiceWebClient;

    @Autowired
    public WalletServiceClient(WebClient walletServiceWebClient) {
        this.walletServiceWebClient = walletServiceWebClient;
    }

    public void createUserWallet(Long userId) {
        WalletRequest walletRequest = new WalletRequest(userId);

        try {
            // Send POST request to create the wallet
            walletServiceWebClient.post()
                    .uri("/api/v1/wallet/create")
                    .bodyValue(walletRequest)
                    .retrieve()
                    .onStatus(
                            status -> status.isError(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .map(body -> new RuntimeException("Error response from wallet-service: " + body)))
                    .toBodilessEntity()
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create wallet for user ID: " + userId, e);
        }
    }
    
}
