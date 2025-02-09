package pesco.wallet_service.payloads;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pesco.wallet_service.enums.CurrencyType;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExternalTransactionRequest {
    private String accountNumber;
    private BigDecimal amount;
    private String bankcode;
    private String description;
    private String fromUser;
    private CurrencyType currencyType;
    private String transferpin;
}
