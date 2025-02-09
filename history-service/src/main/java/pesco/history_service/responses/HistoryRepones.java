package pesco.history_service.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pesco.history_service.enums.TransactionType;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HistoryRepones {
    private Long id;
    private Long walletId;
    private String amount;
    private TransactionType type;
    private String description;
    private String message;
    private String sessionId;
    private String status;
    private LocalDateTime createdOn;
}
