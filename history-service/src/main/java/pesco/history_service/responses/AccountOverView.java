package pesco.history_service.responses;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountOverView {
    private Long Id;
    private String balance;
    private List<HistoryRepones> transactions;
}
