package pesco.authentication_service.servicesImplementation;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import pesco.authentication_service.models.AuthorizeUserVerification;
import pesco.authentication_service.repositories.AuthorizeUserVerificationRepository;
import pesco.authentication_service.services.AuthorizeUserVerificationService;

@Service
@RequiredArgsConstructor
public class AuthorizeUserVerificationServiceImplementation implements AuthorizeUserVerificationService {

    private final AuthorizeUserVerificationRepository authorizeUserVerificationRepository;

    @Override
    public void save(Long userId, Long id) {
        AuthorizeUserVerification auth = new AuthorizeUserVerification();
        auth.setId(id);
        auth.setUserId(userId);
        authorizeUserVerificationRepository.save(auth);
    }

}
