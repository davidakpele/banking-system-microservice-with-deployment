package pesco.history_service.repository;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import pesco.history_service.enums.CurrencyType;
import pesco.history_service.enums.TransactionType;
import pesco.history_service.models.History;

public interface HistoryRepository extends JpaRepository<History, Long> {
    
    @Query("SELECT h FROM History h WHERE h.walletId=:walletId")
    List<History> findByWalletId(Long walletId);

    @Query("SELECT h FROM History h WHERE h.walletId = :walletId AND h.userId = :userId")
    List<History> findByWalletIdAndWalletUserId(Long walletId, Long userId);

    @Query("SELECT COUNT(h) FROM History h WHERE h.userId = :userId")
    Long countByUserId(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(h.amount), 0) FROM History h WHERE h.walletId = :walletId AND h.type = :transactionType AND h.createdOn BETWEEN :startDate AND :endDate")
    BigDecimal calculateTotalSpent(@Param("walletId") Long walletId,
            @Param("transactionType") TransactionType transactionType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Modifying
    @Transactional
    @Query("DELETE FROM History h WHERE h.id IN :ids")
    int deleteByIdIn(List<Long> ids);

    @Query("SELECT h FROM History h WHERE h.walletId=:walletId AND h.userId=:userId")
    List<History> findByWalletIdAndUserId(@Param("walletId") Long walletId, @Param("userId") Long userId);

    @Query("SELECT h FROM History h WHERE h.userId = :userId AND h.createdOn >= :minusMinutes ORDER BY h.createdOn DESC")
    List<History> findRecentTransactionsByUserId(@Param("userId") Long userId,
            @Param("minusMinutes") LocalDateTime minusMinutes);

    @Query("SELECT COALESCE(SUM(h.amount), 0) FROM History h WHERE h.walletId = :id AND h.type = :transactionType AND h.currencyType=:currencyType AND h.createdOn BETWEEN :startDate AND :endDate")
    BigDecimal calculateTotalReceived(Long id, TransactionType transactionType, String currencyType, LocalDate startDate, LocalDate endDate);

    @Query(value = "SELECT h FROM History h WHERE h.walletId = ?1 AND h.currencyType=?2")
    List<History> findByWalletAndCurrency(Long walletId, CurrencyType currencyType);

    @Query("SELECT h FROM History h WHERE h.timestamp > :timestamp AND h.walletId = :walletId")
    List<History> findByTimestampAfterAndWalletId(@Param("timestamp") Timestamp timestamp,
                    @Param("walletId") Long walletId);

}
