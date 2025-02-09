package pesco.revenue_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import pesco.revenue_service.payloads.CreateRevenue;
import pesco.revenue_service.services.RevenueService;

@RestController
@AllArgsConstructor
public class RevenueController {

    private final RevenueService revenueService;

    @PostMapping("/add/revenue")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> addToPlatformRevenue(HttpServletRequest httpRequest, Authentication authentication,
            @RequestBody CreateRevenue request) {
        return revenueService.creditRevenue(request);
    }
}
