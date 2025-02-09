package pesco.revenue_service.payloads;

import java.math.BigDecimal;

import pesco.revenue_service.enums.CurrencyType;

public class CreateRevenue {
    private BigDecimal amount;
    private CurrencyType type;

    public CreateRevenue(BigDecimal amount, CurrencyType type) {
        this.amount = amount;
        this.type = type;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public CurrencyType getType() {
        return this.type;
    }

    public void setType(CurrencyType type) {
        this.type = type;
    }
    
}
