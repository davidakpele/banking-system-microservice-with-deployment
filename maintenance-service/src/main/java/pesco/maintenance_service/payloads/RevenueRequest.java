package pesco.maintenance_service.payloads;

import java.math.BigDecimal;

import pesco.maintenance_service.enums.CurrencyType;

public class RevenueRequest {
    private CurrencyType currencyType;
    private BigDecimal amount;


    public RevenueRequest(CurrencyType currencyType, BigDecimal amount) {
        this.currencyType = currencyType;
        this.amount = amount;
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
