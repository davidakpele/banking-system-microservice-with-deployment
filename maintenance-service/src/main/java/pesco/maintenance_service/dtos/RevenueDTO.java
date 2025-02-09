package pesco.maintenance_service.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class RevenueDTO {
    private Long id;
    private List<BalanceDTO> balances;
    private LocalDateTime updatedAt;

    public RevenueDTO(Long id, List<BalanceDTO> balances, LocalDateTime updatedAt) {
        this.id = id;
        this.balances = balances;
        this.updatedAt = updatedAt;
    }

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

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public static class BalanceDTO {
        private String currencyCode;
        private String currencySymbol;
        private BigDecimal balance;

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
