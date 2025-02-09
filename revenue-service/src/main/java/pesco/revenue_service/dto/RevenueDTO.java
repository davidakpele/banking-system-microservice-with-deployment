package pesco.revenue_service.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import pesco.revenue_service.model.Revenue;

public class RevenueDTO {
    private Long id;
    private List<BalanceDTO> balances;
    private LocalDateTime createdOn;
    private LocalDateTime updatedOn;

    // Default constructor (for serialization/deserialization)
    public RevenueDTO() {
    }

    // Constructor that maps Revenue to RevenueDTO
    public RevenueDTO(Revenue revenue) {
        this.id = revenue.getId();
        this.createdOn = revenue.getCreatedOn();
        this.updatedOn = revenue.getUpdatedOn();

        this.balances = revenue.getBalances().stream()
                .map(balance -> new BalanceDTO(balance.getCurrencySymbol(), balance.getCurrencySymbol(),
                        balance.getBalance()))
                .collect(Collectors.toList());
    }

    // Getters and Setters
    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<BalanceDTO> getBalances() {
        return this.balances;
    }

    public void setBalances(List<BalanceDTO> balances) {
        this.balances = balances;
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

    public static class BalanceDTO {
        private String currencyCode;
        private String currencySymbol;
        private BigDecimal balance;

        // Default constructor (for serialization/deserialization)
        public BalanceDTO() {
        }

        // Constructor for mapping
        public BalanceDTO(String currencyCode, String currencySymbol, BigDecimal balance) {
            this.currencyCode = currencyCode;
            this.currencySymbol = currencySymbol;
            this.balance = balance;
        }

        // Getters and Setters
        public String getCurrencyCode() {
            return currencyCode;
        }

        public void setCurrencyCode(String currencyCode) {
            this.currencyCode = currencyCode;
        }

        public String getCurrencySymbol() {
            return currencySymbol;
        }

        public void setCurrencySymbol(String currencySymbol) {
            this.currencySymbol = currencySymbol;
        }

        public BigDecimal getBalance() {
            return balance;
        }

        public void setBalance(BigDecimal balance) {
            this.balance = balance;
        }
    }
}
