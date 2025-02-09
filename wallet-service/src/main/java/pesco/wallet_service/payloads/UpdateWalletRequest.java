package pesco.wallet_service.payloads;

import java.math.BigDecimal;
import pesco.wallet_service.enums.CurrencyType;

public class UpdateWalletRequest {
    private Long userId;
    private CurrencyType currencyType;
    private BigDecimal amount;

    public UpdateWalletRequest(Long userId, BigDecimal amount, CurrencyType currencyType) {
        this.userId = userId;
        this.amount = amount;
        this.currencyType = currencyType;
    }

    public Long getUserId() {
        return this.userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public CurrencyType getCurrencyType() {
        return this.currencyType;
    }

    public void setCurrencyType(CurrencyType currencyType) {
        this.currencyType = currencyType;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

}
