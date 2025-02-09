package pesco.history_service.models;

import jakarta.persistence.*;
import lombok.*;
import pesco.history_service.enums.CurrencyType;
import pesco.history_service.enums.TransactionType;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "history")
public class History {
    @Id
    private Long id;

    @Column(nullable = false)
    private Long walletId; 

    @Column(nullable = false)
    private Long userId; 

    private String sessionId;

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    private String description;

    private String message;
     
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CurrencyType currencyType; 

    private String status;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    private Timestamp timestamp;
    
    @CreationTimestamp
    private LocalDateTime createdOn;

    @UpdateTimestamp
    private LocalDateTime updatedOn;
}
