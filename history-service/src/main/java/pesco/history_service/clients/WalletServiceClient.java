package pesco.history_service.clients;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import pesco.history_service.dtos.UserDTO;
import pesco.history_service.dtos.WalletDTO;

@Service
public class WalletServiceClient {

    private final WebClient webClient;

    @Autowired
    public WalletServiceClient(WebClient.Builder webClientBuilder, @Value("${wallet-service.base-url}") String baseUrl) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
    }

    public WalletDTO getWalletByUser(UserDTO user, String token) {
        WalletDTO walletDTO = this.webClient.get()
                .uri("/api/v1/wallet/userId/{userId}", user.getId())
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(WalletDTO.class)
                .block();
        return walletDTO;
    }
}
