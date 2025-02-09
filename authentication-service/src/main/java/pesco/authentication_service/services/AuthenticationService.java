package pesco.authentication_service.services;

import java.util.Optional;
import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpServletResponse;
import pesco.authentication_service.models.Users;
import pesco.authentication_service.payloads.UserSignInRequest;
import pesco.authentication_service.payloads.UserSignUpRequest;
import pesco.authentication_service.responses.VerificationTokenResult;

public interface AuthenticationService {

    ResponseEntity<?> createAccount(UserSignUpRequest request);

    ResponseEntity<?> login(UserSignInRequest request, HttpServletResponse response);

    Optional<Users> findByEmail(String email);

    Optional<Users> findByUsername(String username);

    ResponseEntity<?> verifyUser(String token, Long id);

    VerificationTokenResult generateVerificationToken(String oldToken);
}
