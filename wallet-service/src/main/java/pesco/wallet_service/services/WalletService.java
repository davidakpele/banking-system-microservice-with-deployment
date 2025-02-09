package pesco.wallet_service.services;

import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import pesco.wallet_service.enums.CurrencyType;
import pesco.wallet_service.models.Wallet;
import pesco.wallet_service.payloads.CreateWalletRequest;
import pesco.wallet_service.payloads.DeductAmountRequest;
import pesco.wallet_service.payloads.TransactionRequest;
import pesco.wallet_service.payloads.UpdateWalletRequest;

public interface WalletService {
       
        Optional<Wallet> GetWalletByUserId(Long userId, String token);

        ResponseEntity<?> CreateUserTransferPin(TransactionRequest request, String token, Authentication authentication);

        ResponseEntity<?> GetBalance(String username, String token);

        ResponseEntity<?> GetBalanceByCurrencyType(Long userId, CurrencyType currency, String token);

        ResponseEntity<?> createWallet(CreateWalletRequest request);

        ResponseEntity<Optional<Wallet>> findByUserId(Long id);

        ResponseEntity<?> updateUserWallet(UpdateWalletRequest request);

        ResponseEntity<?> deductUserWallet(DeductAmountRequest request);

        ResponseEntity<?> findById(Long walletId);

}