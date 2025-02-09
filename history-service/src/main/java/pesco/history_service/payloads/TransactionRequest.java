package pesco.history_service.payloads;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pesco.history_service.enums.CurrencyType;
import pesco.history_service.enums.TransactionStatus;
import pesco.history_service.enums.TransactionType;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionRequest {
    // history payloads
    private Long senderId;
    private Long senderWalletId;
    private Long recipientId;
    private Long recipientWalletId;
    private BigDecimal amount;
    private String description;
    private String message;
    private TransactionStatus status;
    private TransactionType type;
    private CurrencyType currencyType;
}
