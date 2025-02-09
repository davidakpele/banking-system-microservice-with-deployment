package pesco.history_service.payloads;

import java.math.BigDecimal;
import pesco.history_service.enums.CurrencyType;
import pesco.history_service.enums.TransactionType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FeaturesHistoryRequest {
    private Long userId;
    private BigDecimal amount;
    private CurrencyType currencyType;
    private String description;
    private String ip_address;
    private String message;
    private Long walletId;
    private TransactionType type;

    public FeaturesHistoryRequest() {
    }
    
    public FeaturesHistoryRequest(Long userId, BigDecimal amount, CurrencyType currencyType, String description, String ip_address, String message, Long walletId, TransactionType type) {
        this.userId = userId;
        this.amount = amount;
        this.currencyType = currencyType;
        this.description = description;
        this.ip_address = ip_address;
        this.message = message;
        this.walletId = walletId;
        this.type = type;
    }

    public Long getUserId() {
        return this.userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public CurrencyType getCurrencyType() {
        return this.currencyType;
    }

    public void setCurrencyType(CurrencyType currencyType) {
        this.currencyType = currencyType;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIp_address() {
        return this.ip_address;
    }

    public void setIp_address(String ip_address) {
        this.ip_address = ip_address;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getWalletId() {
        return this.walletId;
    }

    public void setWalletId(Long walletId) {
        this.walletId = walletId;
    }

    public TransactionType getType() {
        return this.type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

}
