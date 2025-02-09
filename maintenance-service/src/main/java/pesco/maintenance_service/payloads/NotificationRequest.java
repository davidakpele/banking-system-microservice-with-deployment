package pesco.maintenance_service.payloads;

import java.math.BigDecimal;

public class NotificationRequest {
    private String firstName;
    private String email;
    private BigDecimal amount;
    private BigDecimal balance;
    private String content;
    private String currencyType;


    public NotificationRequest(String firstName, String email, BigDecimal amount, BigDecimal balance, String content, String currencyType) {
        this.firstName = firstName;
        this.email = email;
        this.amount = amount;
        this.balance = balance;
        this.content = content;
        this.currencyType = currencyType;
    }


    public String getFirstName() {
        return this.firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getBalance() {
        return this.balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCurrencyType() {
        return this.currencyType;
    }

    public void setCurrencyType(String currencyType) {
        this.currencyType = currencyType;
    }

}
