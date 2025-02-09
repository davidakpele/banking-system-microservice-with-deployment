package pesco.revenue_service.model;

import jakarta.persistence.*;
import lombok.*;
import pesco.revenue_service.enums.CurrencyType;
import pesco.revenue_service.enums.TransactionType;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "revenue_transactions")
public class RevenueTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "revenue_id", nullable = false)
    private Revenue revenue;

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private CurrencyType wallet;

    @CreationTimestamp
    private LocalDateTime createdOn;
}
