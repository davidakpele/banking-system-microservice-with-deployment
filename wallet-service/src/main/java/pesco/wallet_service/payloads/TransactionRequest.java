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
public class TransactionRequest {
    // transaction payloads
    private String fromUser;
    private String toUser;
    private BigDecimal amount;
    private String description;
    private String region;
    private CurrencyType currencyType;
    private String transferpin;

    // credit crypto user payloads
    private Long recipientUserId;
    private Long creditorUserId;
    private BigDecimal profit;
}