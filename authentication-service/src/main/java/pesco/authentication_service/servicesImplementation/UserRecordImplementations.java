package pesco.authentication_service.servicesImplementation;

import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import pesco.authentication_service.dtos.UserDTO;
import pesco.authentication_service.models.UserRecord;
import pesco.authentication_service.models.Users;
import pesco.authentication_service.repositories.UserRecordRepository;
import pesco.authentication_service.repositories.UsersRepository;
import pesco.authentication_service.services.UserRecordService;


@Service
public class UserRecordImplementations implements UserRecordService {

    private final UsersRepository userRepository;
    private final UserRecordRepository userRecordRepository;

    public UserRecordImplementations(UsersRepository userRepository, UserRecordRepository userRecordRepository) {
        this.userRepository = userRepository;
        this.userRecordRepository = userRecordRepository;
    }

    @Override
    public UserDTO getUserDetailsById(Long id, Authentication authentication) {
        Users user = userRepository.findUserWithRecordById(id);
        // Get the username from authentication
        String username = authentication.getName();
        if (user == null || !user.getUsername().equals(username)) {
            return null;
        }
        return UserDTO.fromEntity(user);
    }

    @Override
    public UserDTO getUserByUsername(String username, Authentication authentication) {
        Optional<Users> GetUser = userRepository.findByUsername(username);
        // Get the username from authentication
        String NewUsername = authentication.getName();
        if (GetUser.isPresent() && NewUsername.equals(GetUser.get().getUsername())) {
            Users user = userRepository.findUserWithRecordById(GetUser.get().getId());
            return UserDTO.fromEntity(user);
        }

        return null;
    }

    @Override
    public Optional<UserRecord> getUserReferralCode(Long id) {
        var userRecord = userRecordRepository.findByUserId(id);
        return userRecord;
    }

    @Override
    public boolean isLockedAccount(Long userId) {
        return userRecordRepository.isUserAccountLocked(userId);
    }

    @Override
    public boolean isBlockedAccount(Long userId) {
        return userRecordRepository.isUserAccountBlocked(userId);
    }

    @Override
    public Optional<UserRecord> getUserNames(Long userId) {
        return userRecordRepository.findByUserId(userId);
    }

    @Override
    public ResponseEntity<?> updateUserRecordTransferPinStatus(Long id, Authentication authentication) {
        Optional<Users> GetUser = userRepository.findById(id);
        // Get the username from authentication
        String NewUsername = authentication.getName();
        if (GetUser.isPresent() && NewUsername.equals(GetUser.get().getUsername())) {
            Optional<UserRecord> userRecord = userRecordRepository.findByUserId(id);
            if (userRecord.isPresent()) {
                UserRecord updateUserRecord = userRecord.get();
                updateUserRecord.setTransferPinSet(true);
                userRecordRepository.save(updateUserRecord);
            }
            return ResponseEntity.ok("Transfer Pin set successfully.");
        }

        return null;
    }

    @Override
    public UserDTO findPublicUserByUsername(String username) {
        Optional<Users> GetUser = userRepository.findByUsername(username);
        if (GetUser.isPresent()) {
            Users user = userRepository.findUserWithRecordById(GetUser.get().getId());
            return UserDTO.fromEntity(user);
        }

        return null;
    }

    @Override
    public UserDTO findPublicUserByUserId(Long userId) {
        Optional<Users> GetUser = userRepository.findById(userId);
        if (GetUser.isPresent()) {
            Users user = userRepository.findUserWithRecordById(GetUser.get().getId());
            return UserDTO.fromEntity(user);
        }

        return null;
    }

    @Override
    public ResponseEntity<?> lockUserAccount(Long userId) {
        Optional<UserRecord> user = userRecordRepository.findByUserId(userId);
        if (user != null && user.isPresent()) {
            UserRecord updateUserAccount = user.get();

            updateUserAccount.setLocked(true);
            userRecordRepository.save(updateUserAccount);
            return ResponseEntity.ok("User account successfully lock.");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
    }

    @Override
    public ResponseEntity<?> blockUserAccount(Long userId) {
        Optional<UserRecord> user = userRecordRepository.findByUserId(userId);
        if (user != null && user.isPresent()) {
            UserRecord updateUserAccount = user.get();
            updateUserAccount.setBlocked(true);
            userRecordRepository.save(updateUserAccount);
            return ResponseEntity.ok("User account successfully block.");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
    }

}
