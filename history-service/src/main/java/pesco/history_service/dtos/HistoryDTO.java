package pesco.history_service.dtos;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import pesco.history_service.enums.CurrencyType;
import pesco.history_service.enums.TransactionType;
import pesco.history_service.models.History;

public class HistoryDTO {
    private Long id;
    private Long walletId;
    private Long userId;
    private String sessionId;
    private BigDecimal amount;
    private TransactionType type;
    private String description;
    private String message;
    private CurrencyType currencyType;
    private String status;
    private String ipAddress;
    private Timestamp timestamp;
    private LocalDateTime createdOn;
    private LocalDateTime updatedOn;

    // Constructor to convert a History entity to a DTO
    public HistoryDTO(History history) {
        this.id = history.getId();
        this.walletId = history.getWalletId();
        this.userId = history.getUserId();
        this.sessionId = history.getSessionId();
        this.amount = history.getAmount();
        this.type = history.getType();
        this.description = history.getDescription();
        this.message = history.getMessage();
        this.currencyType = history.getCurrencyType();
        this.status = history.getStatus();
        this.ipAddress = history.getIpAddress();
        this.timestamp = history.getTimestamp();
        this.createdOn = history.getCreatedOn();
        this.updatedOn = history.getUpdatedOn();
    }

    // No-args constructor for serialization/deserialization
    public HistoryDTO() {
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getWalletId() {
        return walletId;
    }

    public void setWalletId(Long walletId) {
        this.walletId = walletId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public CurrencyType getCurrencyType() {
        return currencyType;
    }

    public void setCurrencyType(CurrencyType currencyType) {
        this.currencyType = currencyType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(LocalDateTime createdOn) {
        this.createdOn = createdOn;
    }

    public LocalDateTime getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(LocalDateTime updatedOn) {
        this.updatedOn = updatedOn;
    }
}
