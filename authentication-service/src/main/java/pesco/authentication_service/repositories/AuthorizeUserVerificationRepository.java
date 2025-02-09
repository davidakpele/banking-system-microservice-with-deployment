package pesco.authentication_service.repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pesco.authentication_service.models.AuthorizeUserVerification;

@Repository
public interface AuthorizeUserVerificationRepository extends JpaRepository<AuthorizeUserVerification, Long> {

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM AuthorizeUserVerification a INNER JOIN Users u ON a.userId = u.id WHERE u.enabled = true AND a.id = :id")
    boolean findUserById(Long id);

    @Query("select t from AuthorizeUserVerification t where t.userId = :id")
    Optional<AuthorizeUserVerification> findUserByIdOptional(Long id);

}
