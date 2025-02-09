package pesco.withdrawal_service.dto;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import pesco.withdrawal_service.enums.CurrencyType;
import pesco.withdrawal_service.enums.TransactionType;

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

    public HistoryDTO() {
    }

    public HistoryDTO(Long id, Long walletId, Long userId, String sessionId, BigDecimal amount, TransactionType type, String description, String message, CurrencyType currencyType, String status, String ipAddress, Timestamp timestamp, LocalDateTime createdOn, LocalDateTime updatedOn) {
        this.id = id;
        this.walletId = walletId;
        this.userId = userId;
        this.sessionId = sessionId;
        this.amount = amount;
        this.type = type;
        this.description = description;
        this.message = message;
        this.currencyType = currencyType;
        this.status = status;
        this.ipAddress = ipAddress;
        this.timestamp = timestamp;
        this.createdOn = createdOn;
        this.updatedOn = updatedOn;
    }


    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getWalletId() {
        return this.walletId;
    }

    public void setWalletId(Long walletId) {
        this.walletId = walletId;
    }

    public Long getUserId() {
        return this.userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getSessionId() {
        return this.sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public TransactionType getType() {
        return this.type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public CurrencyType getCurrencyType() {
        return this.currencyType;
    }

    public void setCurrencyType(CurrencyType currencyType) {
        this.currencyType = currencyType;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getIpAddress() {
        return this.ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Timestamp getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public LocalDateTime getCreatedOn() {
        return this.createdOn;
    }

    public void setCreatedOn(LocalDateTime createdOn) {
        this.createdOn = createdOn;
    }

    public LocalDateTime getUpdatedOn() {
        return this.updatedOn;
    }

    public void setUpdatedOn(LocalDateTime updatedOn) {
        this.updatedOn = updatedOn;
    }

}
