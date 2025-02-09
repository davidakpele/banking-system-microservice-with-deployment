package pesco.deposit_service.payloads;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pesco.deposit_service.enums.CurrencyType;
import pesco.deposit_service.enums.DEPOSITANDWITHDRAWALSYSTEM;
import pesco.deposit_service.enums.TransactionType;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DepositRequest {
    private String accountHolderName;
    private String accountNumber;
    private String bankCode;
    private String bankName;
    private Long userId;
    private Long walletId;
    private BigDecimal amount;
    private TransactionType type;
    private CurrencyType currencyType;
    private DEPOSITANDWITHDRAWALSYSTEM depositSystem;
}