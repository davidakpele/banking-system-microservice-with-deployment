package pesco.notification_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import pesco.notification_service.messageProducer.AuthenticationMessageProducer;
import pesco.notification_service.messageProducer.WalletMessageProducer;
import pesco.notification_service.payloads.AccountVerificationRequest;
import pesco.notification_service.payloads.CreditWalletNotification;
import pesco.notification_service.payloads.DebitWalletNotification;
import pesco.notification_service.payloads.DepositWalletNotification;
import pesco.notification_service.payloads.MaintenanceDeductionNotification;
import pesco.notification_service.payloads.PasswordResetRequest;
import pesco.notification_service.payloads.UserOTPMessage;

@RestController
@RequiredArgsConstructor
@Validated
public class MessageController {
    
    private final AuthenticationMessageProducer authenticationMessageProducer;
    private final WalletMessageProducer walletMessageProducer;

    @PostMapping("/send/verification-message")
    public ResponseEntity<?> verificationAlert(HttpServletRequest httpRequest,
            @Valid @RequestBody AccountVerificationRequest request) {
        try {
            authenticationMessageProducer.sendVerificationEmail(request.getEmail(), request.getmessage(), request.getLink(), request.getUsername());
            return ResponseEntity.ok().body("Verification email has been successfully sent.!");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to send Verification email.");
        }
    }

    @PostMapping("/send/otp-message")
    public ResponseEntity<?> sendUserOtpAlert(HttpServletRequest httpRequest,
            @Valid @RequestBody UserOTPMessage request) {
        try {
            authenticationMessageProducer.sendOptEmail(request.getEmail(), request.getOtp(), request.getRestPassword(), request.getConfigTwoFactorAuth(), request.getConfigTwoFactorAuthRecovery());
            return ResponseEntity.ok().body("OTP successfully sent.!");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to send OTP.");
        }
    }

    @PostMapping("/send/password-reset-message")
    public ResponseEntity<?> sendUserResetPasswordAlert(HttpServletRequest httpRequest,
            @Valid @RequestBody PasswordResetRequest request) {
        try {
            authenticationMessageProducer.sendPasswordResetEmail(request.getEmail(), request.getUsername(), request.getmessage(), request.getUrl(), request.getCustomerEmail());;
            return ResponseEntity.ok().body("Password reset link successfully sent.!");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to send Password reset.");
        }
    }

    // wallet alert
    @PostMapping("/send/credit-wallet-message")
    public ResponseEntity<?> sendCreditWalletAlert(HttpServletRequest httpRequest,
            @Valid @RequestBody CreditWalletNotification request) {
        try {
            walletMessageProducer.sendCreditWalletNotification(request.getRecipientEmail(), request.getTransferAmount(), request.getSenderFullName(), request.getReceiverFullName(), request.getRecipientTotalBalance(), request.getCurrency());
            return ResponseEntity.ok().body("Credit message successfully sent.!");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to send Credit message.");
        }
    }

    @PostMapping("/send/deposit-wallet-message")
    public ResponseEntity<?> sendDepositWalletAlert(HttpServletRequest httpRequest,
            @Valid @RequestBody DepositWalletNotification request) {
        try {
            walletMessageProducer.sendDepositWalletNotification(request.getRecipientEmail(), request.getRecipientName(),
                    request.getDepositAmount(), request.getTransactionTime(), request.getTotalBalance());
            return ResponseEntity.ok().body("Deposit message successfully sent.!");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to send Deposit message.");
        }
    }
    
    
    @PostMapping("/send/debit-wallet-message")
    public ResponseEntity<?> sendDebitWalletAlert(HttpServletRequest httpRequest,
            @Valid @RequestBody DebitWalletNotification request) {
        try {
            walletMessageProducer.sendDebitWalletNotification(request.getSenderEmail(), request.getFeeAmount(), request.getTransferAmount(), request.getSenderFullName(), request.getReceiverFullName(), request.getBalance(), request.getCurrency());
            return ResponseEntity.ok().body("Debit message successfully sent.!");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to send Debit message.");
        }
    }


    @PostMapping("/send/matainance-wallet-message")
    public ResponseEntity<?> sendMaintenanceWalletAlert(HttpServletRequest httpRequest,
            @Valid @RequestBody MaintenanceDeductionNotification request) {
        try {
            walletMessageProducer.sendMaintenanceNotification(request.getEmail(), request.getFirstName(), request.getAmount(), request.getBalance(), request.getContent());
            return ResponseEntity.ok().body("Wallet maintenance message successfully sent.!");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to send Wallet maintenance message.");
        }
    }

}
