package pesco.authentication_service.services;

import org.springframework.http.ResponseEntity;
import pesco.authentication_service.enums.AttemptType;

public interface UserAttemptService {

    ResponseEntity<?> createFailAttempt(Long id, AttemptType login);

    ResponseEntity<?> UpdateUserAccount(Long id);

}
