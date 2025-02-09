package pesco.revenue_service.model;

import java.math.BigDecimal;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CurrencyBalance {
    @Column(name = "currency_code", nullable = false)
    private String currencyCode;

    @Column(name = "currency_symbol", nullable = false)
    private String currencySymbol; 

    @Column(name = "balance", nullable = false)
    private BigDecimal balance;
}
