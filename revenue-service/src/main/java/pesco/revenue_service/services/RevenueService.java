package pesco.revenue_service.services;

import org.springframework.http.ResponseEntity;
import pesco.revenue_service.dto.RevenueDTO;
import pesco.revenue_service.payloads.CreateRevenue;

public interface RevenueService {
    ResponseEntity<?> creditRevenue(CreateRevenue revenue);

    RevenueDTO getRevenueByUser(Long userId);
}
