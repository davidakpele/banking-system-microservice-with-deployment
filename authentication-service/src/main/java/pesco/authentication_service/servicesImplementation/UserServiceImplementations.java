package pesco.authentication_service.servicesImplementation;

import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import pesco.authentication_service.clients.NotificationServiceClient;
import pesco.authentication_service.exceptions.Error;
import pesco.authentication_service.models.UserRecord;
import pesco.authentication_service.models.Users;
import pesco.authentication_service.repositories.UserRecordRepository;
import pesco.authentication_service.repositories.UsersRepository;
import pesco.authentication_service.services.PasswordResetTokenService;
import pesco.authentication_service.services.UserService;
import pesco.authentication_service.utils.CustomerServiceEmailProperty;
import pesco.authentication_service.utils.KeyWrapper;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImplementations implements UserService {

    private final NotificationServiceClient notificationServiceClient;
    private final UsersRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenService passwordResetTokenService;;
    private final CustomerServiceEmailProperty customerServiceEmailProperty;
    private final KeyWrapper keysWrapper;
    private final UserRecordRepository userRecordRepository;

    public Users getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    public Users getUserById(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }

    public Optional<Users> findById(Long id) {
        return userRepository.findById(id);
    }

    public ResponseEntity<?> resetPassword(Long userId, String password, Authentication authentication) {
        Users user = userRepository.findById(userId).orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid request: User not found");
        }

        String authenticatedUsername = authentication.getName();
        if (!user.getUsername().equals(authenticatedUsername)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid request: Unauthorized to reset password");
        }

        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
        return ResponseEntity.ok("Password reset successful");
    }

    public Object deactivateAccount(Long id, Authentication authentication) {
        Users user = userRepository.findById(id).orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid request: User not found");
        }

        String authenticatedUsername = authentication.getName();
        if (!user.getUsername().equals(authenticatedUsername)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid request: Unauthorized to reset password");
        }
        user.setEnabled(false);
        userRepository.save(user);
        return ResponseEntity.ok("Account deactivated successfully");
    }

    public ResponseEntity<?> forgetPassword(String email) {
        Map<String, Object> response = new HashMap<>();
        Optional<Users> user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            String token = UUID.randomUUID().toString();

            passwordResetTokenService.createUserUserSession(user.get().getId(), token);
            // Send email
            String url = keysWrapper.getUrl() + "/auth/reset-password?token=" + token;
            String content = "We received a request to reset the password for your account associated with this email address. If you did not request this change, please ignore this email."
                    + "To reset your password, please click on the link below:";
            String customerEmail = customerServiceEmailProperty.getEmail();

            CompletableFuture<Void>sendResetPasswordMessage = CompletableFuture.runAsync(() -> notificationServiceClient.sendPasswordResetMessage(user.get().getEmail(), user.get().getUsername(), content, url,customerEmail));
            sendResetPasswordMessage.join();
        
            response.put("message",
                    "Message has been sent to the very email address provided. Please follow the instructions to reset your password.");
            response.put("token", token);
            response.put("status", HttpStatus.OK);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        response.put("message", "User not found.!");
        response.put("status", HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    public ResponseEntity<?> updateUserProfile(String username, String email, String gender, String profilePath) {
        Optional<Users> optionalUser = userRepository.findByUsername(username);
        Optional<UserRecord> optionalUserRecord = userRecordRepository.findByUserId(optionalUser.get().getId());

        if (!optionalUser.isPresent()) {
            return Error.createResponse(
                    "UNAUTHORIZE ACCESS", HttpStatus.FORBIDDEN,
                    "You dont have access to the endpoints");
        }
        Users user = optionalUser.get();
        user.setUsername(username);
        user.setEmail(email);

        UserRecord record = optionalUserRecord.get();

        if (profilePath != null && profilePath != "") {
            record.setPhoto(profilePath);
        }
        record.setGender(gender);
        userRepository.save(user);

        userRecordRepository.save(record);

        return ResponseEntity.ok().body("User updated successfully");
    }

}
