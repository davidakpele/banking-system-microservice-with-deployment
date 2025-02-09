package pesco.revenue_service.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pesco.revenue_service.model.RevenueTransaction;

@Repository
public interface RevenueTransactionRepository extends JpaRepository<RevenueTransaction, Long> {
    List<RevenueTransaction> findByRevenue_Id(Long revenueId);
}
