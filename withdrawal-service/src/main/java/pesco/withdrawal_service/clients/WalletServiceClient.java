package pesco.withdrawal_service.clients;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import jakarta.servlet.http.HttpServletRequest;
import pesco.withdrawal_service.dto.WalletDTO;
import pesco.withdrawal_service.enums.CurrencyType;
import pesco.withdrawal_service.payloads.CreateWalletRequest;
import pesco.withdrawal_service.payloads.DeductAmountRequest;
import pesco.withdrawal_service.payloads.UpdateWalletRequest;
import reactor.core.publisher.Mono;

@Service
public class WalletServiceClient {

    private final WebClient walletServiceWebClient;

    @Autowired
    public WalletServiceClient(WebClient walletServiceWebClient) {
        this.walletServiceWebClient = walletServiceWebClient;
    }

    public WalletDTO findByUserId(Long userId,  String token) {
        WalletDTO walletDTO = this.walletServiceWebClient.get()
            .uri("/api/v1/wallet/userId/{userId}", userId)
            .header("Authorization", "Bearer " + token)
            .retrieve()
            .bodyToMono(WalletDTO.class)
            .block();
        return walletDTO;
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

    public static BigDecimal parseFormattedString(String formattedValue) throws ParseException {
        String pattern = "#,##0.00";
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator(',');
        symbols.setDecimalSeparator('.');
        DecimalFormat decimalFormat = new DecimalFormat(pattern, symbols);
        
        Number number = decimalFormat.parse(formattedValue);
        return new BigDecimal(number.toString());
    }

    public WalletDTO createUserWallet(Long id) {
        // Create the request body
        CreateWalletRequest request = new CreateWalletRequest(id);
        WalletDTO walletDTO = this.walletServiceWebClient.post()
                .uri("/api/v1/wallet/create")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(WalletDTO.class)
                .block();

        return walletDTO;
    }

    public Object updateUserWallet(Long id, BigDecimal amount, CurrencyType currencyType, String token) {
        UpdateWalletRequest request = new UpdateWalletRequest(id, amount, currencyType);
        return this.walletServiceWebClient.put()
                .uri("/api/v1/wallet/update")
                .header("Authorization", "Bearer " + token) 
                .bodyValue(request) 
                .retrieve()
                .bodyToMono(WalletDTO.class) 
                .block();
    }

    public Object DeductAmountFromSenderWallet(Long id, CurrencyType currencyType, String token, BigDecimal finalDeduction) {
        DeductAmountRequest request = new DeductAmountRequest(id, currencyType, finalDeduction);

        return this.walletServiceWebClient.put()
                .uri("/api/v1/wallet/deduct/userId/{userId}", id)
                .header("Authorization", "Bearer " + token)
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> response.bodyToMono(String.class)
                        .flatMap(errorBody -> {
                            System.err.println("Error Response: " + errorBody);
                            return Mono.error(new RuntimeException("Wallet deduction failed: " + errorBody));
                        }))
                .bodyToMono(String.class) 
                .block();
    }

    public String getClientIp(HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }


}
