package pesco.withdrawal_service.payloads;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;
import pesco.withdrawal_service.enums.CurrencyType;

@Data
@Builder
public class WithdrawInRequest {
    private String senderUser;
    private String recipientUser;
    private BigDecimal amount;
    private String description;
    private String region;
    private CurrencyType currencyType;
    private String transferpin;

    public WithdrawInRequest() {
    }

    public WithdrawInRequest(String senderUser, String recipientUser, BigDecimal amount, String description, String region, CurrencyType currencyType, String transferpin) {
        this.senderUser = senderUser;
        this.recipientUser = recipientUser;
        this.amount = amount;
        this.description = description;
        this.region = region;
        this.currencyType = currencyType;
        this.transferpin = transferpin;
    }


    public String getSenderUser() {
        return this.senderUser;
    }

    public void setSenderUser(String senderUser) {
        this.senderUser = senderUser;
    }

    public String getRecipientUser() {
        return this.recipientUser;
    }

    public void setRecipientUser(String recipientUser) {
        this.recipientUser = recipientUser;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRegion() {
        return this.region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public CurrencyType getCurrencyType() {
        return this.currencyType;
    }

    public void setCurrencyType(CurrencyType currencyType) {
        this.currencyType = currencyType;
    }

    public String getTransferpin() {
        return this.transferpin;
    }

    public void setTransferpin(String transferpin) {
        this.transferpin = transferpin;
    }

}
