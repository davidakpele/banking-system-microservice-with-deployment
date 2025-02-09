package pesco.maintenance_service.enums;

public enum DebtStatus {
    PENDING, // Debt is active and waiting for payment
    PAID, // Debt is fully settled
    OVERDUE // Debt has passed the due date and is still unpaid
}