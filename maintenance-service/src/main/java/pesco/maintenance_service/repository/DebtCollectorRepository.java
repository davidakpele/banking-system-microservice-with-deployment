package pesco.maintenance_service.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pesco.maintenance_service.enums.DebtStatus;
import pesco.maintenance_service.model.DebtCollector;
@Repository
public interface DebtCollectorRepository extends JpaRepository<DebtCollector, Long> {

    @Query("SELECT dc FROM DebtCollector dc WHERE dc.userId = :userId AND dc.debtStatus = :debtStatus")
    Optional<DebtCollector> findByUserIdAndDebtStatus(@Param("userId") Long userId,
            @Param("debtStatus") DebtStatus debtStatus);

    @Query(value = "SELECT dc FROM DebtCollector dc WHERE dc.debtStatus=:pending")
    List<DebtCollector> findByDebtStatus(DebtStatus pending);

    @Modifying
    @Query("DELETE FROM DebtCollector d WHERE d.userId IN :userIds")
    void deleteByUserIds(@Param("userIds") List<Long> userIds);

    @Query(value = "SELECT dc FROM DebtCollector dc WHERE dc.debtStatus=:pending AND dc.currencyType=:currencyType AND dc.userId =:userId")
    Optional<DebtCollector> findByUserIdAndCurrencyTypeAndDebtStatus(Long userId, String currencyType,
            DebtStatus pending);

}
