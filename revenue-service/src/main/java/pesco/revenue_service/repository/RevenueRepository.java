package pesco.revenue_service.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pesco.revenue_service.model.Revenue;

@Repository
public interface RevenueRepository extends JpaRepository<Revenue, Long> {
    // Fetch the first Revenue record by ID in ascending order
    @Query("SELECT r FROM Revenue r ORDER BY r.id ASC LIMIT 1")
    Optional<Revenue> findFirstByOrderByIdAsc();
}
