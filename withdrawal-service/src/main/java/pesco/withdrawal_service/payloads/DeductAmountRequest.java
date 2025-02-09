package pesco.withdrawal_service.payloads;

import java.math.BigDecimal;
import pesco.withdrawal_service.enums.CurrencyType;

public class DeductAmountRequest {
    private Long id;
    private CurrencyType currencyType;
    private BigDecimal amount;

    public DeductAmountRequest(Long id, CurrencyType currencyType, BigDecimal amount) {
        this.id = id;
        this.currencyType = currencyType;
        this.amount = amount;
    }


    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
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
