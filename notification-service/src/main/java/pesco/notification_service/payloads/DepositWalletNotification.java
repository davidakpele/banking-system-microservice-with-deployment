package pesco.notification_service.payloads;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class DepositWalletNotification {
    @NotBlank(message = "Recipient email is mandatory")
    @Email(message = "Invalid email format")
    private String recipientEmail;

    @NotBlank(message = "Recipient name is mandatory")
    private String recipientName;

    @NotNull(message = "Deposit amount is mandatory")
    @Positive(message = "Deposit amount must be positive")
    private BigDecimal depositAmount;

    @NotNull(message = "Transaction time is mandatory")
    @PastOrPresent(message = "Transaction time cannot be in the future")
    private LocalDateTime transactionTime;

    @NotNull(message = "Total balance is mandatory")
    @Positive(message = "Total balance must be positive")
    private BigDecimal totalBalance;


    public DepositWalletNotification() {
    }

    @JsonCreator
    public DepositWalletNotification(
            @JsonProperty("recipientEmail") String recipientEmail,
            @JsonProperty("recipientName") String recipientName,
            @JsonProperty("depositAmount") BigDecimal depositAmount,
            @JsonProperty("transactionTime") LocalDateTime transactionTime,
            @JsonProperty("totalBalance") BigDecimal totalBalance) {
        this.recipientEmail = recipientEmail;
        this.recipientName = recipientName;
        this.depositAmount = depositAmount;
        this.transactionTime = transactionTime;
        this.totalBalance = totalBalance;
    }

    public String getRecipientEmail() {
        return this.recipientEmail;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    public String getRecipientName() {
        return this.recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public BigDecimal getDepositAmount() {
        return this.depositAmount;
    }

    public void setDepositAmount(BigDecimal depositAmount) {
        this.depositAmount = depositAmount;
    }

    public LocalDateTime getTransactionTime() {
        return this.transactionTime;
    }

    public void setTransactionTime(LocalDateTime transactionTime) {
        this.transactionTime = transactionTime;
    }

    public BigDecimal getTotalBalance() {
        return this.totalBalance;
    }

    public void setTotalBalance(BigDecimal totalBalance) {
        this.totalBalance = totalBalance;
    }

}
