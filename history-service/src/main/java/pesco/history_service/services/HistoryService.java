package pesco.history_service.services;

import java.time.Instant;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import jakarta.servlet.http.HttpServletRequest;
import pesco.history_service.dtos.HistoryDTO;
import pesco.history_service.payloads.CreateWalletHistory;
import pesco.history_service.payloads.DepositHistoryRequest;
import pesco.history_service.payloads.FeaturesHistoryRequest;

public interface HistoryService {
    
    ResponseEntity<?> fetchUserHistoryByCurrencyType(String currency, String username, String token);

    ResponseEntity<?> fetchAllUserHistory(String username, String token);

    ResponseEntity<?> createDepositHistory(HttpServletRequest httpRequest, String token, String username, DepositHistoryRequest request);

    ResponseEntity<?> createFeaturessHistory(HttpServletRequest httpRequest, String token, String username, FeaturesHistoryRequest request);

    ResponseEntity<?> createWithdrawalHistory(HttpServletRequest httpRequest, String token, String username, 
            CreateWalletHistory request);

    boolean deleteHistory(List<Long> ids);

    List<HistoryDTO> findByTimestampAfterAndWalletId(Long walletId, Instant timestamp);

    ResponseEntity<?> createUserCreditHistory(HttpServletRequest httpRequest, String token, String username,
            CreateWalletHistory request);

    List<HistoryDTO> findByWalletIdAndUserId(Long walletId, Long userId);

    List<HistoryDTO> findRecentTransactionsByUserId(Long walletId, Instant timestamp);

    ResponseEntity<?> countUserHistory(String token, Authentication authentication);
}
