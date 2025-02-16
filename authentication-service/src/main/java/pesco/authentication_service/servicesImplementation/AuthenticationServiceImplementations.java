package pesco.authentication_service.servicesImplementation;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import pesco.authentication_service.clients.NotificationServiceClient;
import pesco.authentication_service.clients.WalletServiceClient;
import pesco.authentication_service.enums.AttemptType;
import pesco.authentication_service.enums.Role;
import pesco.authentication_service.enums.UserStatus;
import pesco.authentication_service.exceptions.UserNotFoundException;
import pesco.authentication_service.models.AuthorizeUserVerification;
import pesco.authentication_service.models.TwoFactorAuthentication;
import pesco.authentication_service.models.UserRecord;
import pesco.authentication_service.models.Users;
import pesco.authentication_service.models.VerificationToken;
import pesco.authentication_service.payloads.UserSignInRequest;
import pesco.authentication_service.payloads.UserSignUpRequest;
import pesco.authentication_service.repositories.AuthorizeUserVerificationRepository;
import pesco.authentication_service.repositories.UserRecordRepository;
import pesco.authentication_service.repositories.UsersRepository;
import pesco.authentication_service.repositories.VerificationTokenRepository;
import pesco.authentication_service.responses.AuthResponse;
import pesco.authentication_service.responses.VerificationTokenResult;
import pesco.authentication_service.services.AuthenticationService;
import pesco.authentication_service.services.AuthorizeUserVerificationService;
import pesco.authentication_service.services.JwtService;
import pesco.authentication_service.services.UserAttemptService;
import pesco.authentication_service.utils.KeyWrapper;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthenticationServiceImplementations implements AuthenticationService {

    private static final int EXPIRATION_MINUTES = 10;
    private Date expirationTime;
    private final UsersRepository userRepository;
    private final UserRecordRepository userRecordRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final NotificationServiceClient notificationServiceClient;
    private final VerificationTokenRepository verificationTokenRepository;
    private final AuthenticationManager authenticationManager;
    private final KeyWrapper keysWrapper;
    private final AuthorizeUserVerificationService authorizeUserVerificationService;
    private final AuthorizeUserVerificationRepository authorizeUserVerificationRepository;
    private final TwoFactorAuthenticationServiceImplementations twoFactorAuthenticationServiceImplementation;
    private final UserAttemptService userAttemptService;
    private final WalletServiceClient walletServiceClient;

    @Transactional
    public ResponseEntity<String> createAccount(UserSignUpRequest request) {
        Long nextUserId = getNextUserId();

        Users user = Users.builder()
            .id(nextUserId)
            .email(request.getEmail())
            .username(request.getUsername())
            .twoFactorAuth(false)
            .role(Role.USER)
            .password(passwordEncoder.encode(request.getPassword()))
            .build();
        userRepository.save(user);

        // Retrieve the saved user to get the user ID
        Users savedUser = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        // generate referral code
        String referralCode = UUID.randomUUID().toString();

        UserRecord userRecord = UserRecord.builder()
            .user(savedUser)
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .gender(request.getGender())
            .telephone(request.getTelephone())
            .country(request.getCountry())
            .city(request.getCity())
            .isTransferPinSet(false)
            .locked(false)
            .lockedAt(null)
            .referralCode(referralCode)
            .isBlocked(false)
            .totalReferers(null)
            .referralLink(keysWrapper.getUrl() + "/auth/register?referral_code=" + referralCode)
            .status(UserStatus.PENDING_VERIFICATION)
            .build();
        userRecordRepository.save(userRecord);

        // Create a verification token
        UUID verificationToken = UUID.randomUUID();
        expirationTime = calculateExpirationDate(EXPIRATION_MINUTES);
        VerificationToken tokenEntity = VerificationToken.builder()
                .userId(nextUserId)
                .token(String.valueOf(verificationToken))
                .expirationTime(expirationTime)
                .build();
        verificationTokenRepository.save(tokenEntity);
        Long unverifiedUserId = KeyWrapper.generateUniqueAuthorizeUserId();
        authorizeUserVerificationService.save(nextUserId, unverifiedUserId);
        // Generate the verification link using keysWrapper.getUrl()
        String verificationLink = keysWrapper.getUrl() + "/auth/verifyRegistration?token=" + verificationToken + "&id="
                + unverifiedUserId;

        String content = "Dear " + request.getUsername() + ",\n\n"
                + "Thank you for signing up for pesco! We're excited to have you on board.\n\n"
                + "Please verify your email address to complete your registration and activate your account.";

        CompletableFuture<Void>sendVerificationMessage = CompletableFuture.runAsync(() -> notificationServiceClient.sendVerificationEmail(request.getEmail(), content, verificationLink, request.getUsername()));
        sendVerificationMessage.join();

        String message = "Thanks for your interest in joining Artex network! To complete account verification, email has been sent to email address you provided.";
        return new ResponseEntity<>(message, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<?> login(UserSignInRequest request, HttpServletResponse response) {
        Map<String, Object> Authresponse = new HashMap<>();

        Optional<Users> userInfo = userRepository.findByUsername(request.getUsername());
        if (userInfo.get().isEnabled()) {
            try {
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getUsername(),
                                request.getPassword()));
                Users user = userRepository.findById(userInfo.get().getId())
                        .orElseThrow(() -> new UserNotFoundException("User records not found"));

                Optional<UserRecord> checkAccountStatus = userRecordRepository.findByUserId(user.getId());
                if (checkAccountStatus.isPresent()) {
                    if (checkAccountStatus.get().isLocked()) {
                        // Account is not verified
                        Authresponse.put("status", HttpStatus.BAD_REQUEST);
                        Authresponse.put("success", false);
                        Authresponse.put("message",
                                "Sorry, This account is currently Locked, Please contact our customer service.");

                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Authresponse);
                    } else if (checkAccountStatus.get().isBlocked()) {
                        // Account is not verified
                        Authresponse.put("status", false);
                        Authresponse.put("success", false);
                        Authresponse.put("message",
                                "Sorry, This account is currently Blocked, Please contact our customer service.");

                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Authresponse);
                    }
                }
                var jwtToken = jwtService.generateToken((UserDetails) userInfo.get());

                boolean isTwoFactorAuthEnabled = user.isTwoFactorAuth();
                Optional<Users> authUser = userRepository.findByEmail(user.getEmail());

                // Handle Two-Factor Authentication
                if (isTwoFactorAuthEnabled) {
                    Authresponse.put("message", "Two factor authentication is enabled.");
                    Authresponse.put("twoFactorAuthEnabled", isTwoFactorAuthEnabled);
                    Authresponse.put("status", HttpStatus.BAD_REQUEST);

                    String Otp = keysWrapper.generateOTP();
                    String jwt = keysWrapper.generateUniqueKey();

                    TwoFactorAuthentication alreadyExistTwoFactorOTP = twoFactorAuthenticationServiceImplementation
                            .findByUser(user.getId());

                    if (alreadyExistTwoFactorOTP != null) {
                        twoFactorAuthenticationServiceImplementation.deleteTwoFactorOtp(alreadyExistTwoFactorOTP);
                    }

                    Users userOtp = authUser.get();
                    TwoFactorAuthentication newTwoFactorOTP = twoFactorAuthenticationServiceImplementation
                            .createTwoFactorOtp(userOtp, Otp, jwt);

                    Authresponse.put("status", newTwoFactorOTP.getId().toString());

                    TwoFactorAuthentication getToken = twoFactorAuthenticationServiceImplementation
                            .findByUser(user.getId());

                    Authresponse.put("jwt", getToken.getToken());

                    String restPassword = keysWrapper.getUrl() + "/auth/security/password";
                    String configTwoFactorAuth = keysWrapper.getUrl()
                            + "/auth/security/configuring-two-factor-authentication";
                    String configTwoFactorAuthRecovery = keysWrapper.getUrl()
                            + "/auth/security/configuring-two-factor-authentication-recovery-methods";

                    // Send otp to user email
                    CompletableFuture<Void>sendUserOTPMessage = CompletableFuture.runAsync(() -> notificationServiceClient.sendOptEmail(user.getEmail(), Otp, restPassword, configTwoFactorAuth, configTwoFactorAuthRecovery));
                    sendUserOTPMessage.join();
                
                    return new ResponseEntity<>(Authresponse, HttpStatus.OK);
                }

                Authresponse.put("jwt", jwtToken);
                Authresponse.put("email", userInfo.get().getEmail());
                Authresponse.put("userId", userInfo.get().getId());
                Authresponse.put("status", HttpStatus.OK);
                Authresponse.put("success", true);
                Authresponse.put("session", true);
                Authresponse.put("twoFactorAuthEnabled", userInfo.get().isTwoFactorAuth());
                Authresponse.put("username", userInfo.get().getUsername());
                // Set the JWT as a cookie
                Cookie cookie = new Cookie("authToken", jwtToken);
                cookie.setMaxAge(24 * 60 * 60);
                cookie.setPath("/");
                response.addCookie(cookie);

                return new ResponseEntity<>(Authresponse, HttpStatus.OK);
            } catch (Exception e) {
                // Authentication failed, increment bad attempts counter
                ResponseEntity<?> createAttempt = userAttemptService.createFailAttempt(userInfo.get().getId(),
                        AttemptType.LOGIN);

                Authresponse.put("status", HttpStatus.BAD_REQUEST);
                Authresponse.put("success", false);
                Authresponse.put("details", createAttempt.getBody() == null ? "Invalid credentials provided.!"
                        : createAttempt.getBody().toString());

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Authresponse);
            }
        } else {
            // Account is not verified
            Authresponse.put("status", false);
            Authresponse.put("success", false);
            Authresponse.put("message", "This account has not been verified");

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Authresponse);
        }

    }

    @Override
    public Optional<Users> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public Optional<Users> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public VerificationTokenResult generateVerificationToken(String oldToken) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(oldToken);
        if (verificationToken == null) {
            return new VerificationTokenResult(false, "Token not found");
        }

        // Update expirationTime (adjust this based on your requirements)
        Date newExpirationTime = calculateExpirationDate(EXPIRATION_MINUTES);

        Optional<Users> user = userRepository.findById(verificationToken.getUserId());
        verificationToken.setExpirationTime(newExpirationTime);

        String newToken = UUID.randomUUID().toString();
        // Generate a new token
        verificationToken.setToken(newToken);

        // Save the updated verification token
        verificationTokenRepository.save(verificationToken);

        Optional<AuthorizeUserVerification> optionAuthUser = authorizeUserVerificationRepository
                .findUserByIdOptional(user.get().getId());

        String verificationLink = keysWrapper.getUrl() + "/auth/verifyRegistration?token=" + newToken + "&id="
                + optionAuthUser.get().getId();

        String content = "Dear " + user.get().getUsername() + ",\n\n"
                + "Thank you for registering with Pesco! We're thrilled to have you join us.\n\n"
                + "To complete your registration and activate your account";

        CompletableFuture<Void>sendVerificationLinkMessage = CompletableFuture.runAsync(() -> notificationServiceClient.sendVerificationEmail(user.get().getEmail(), content, verificationLink, user.get().getUsername()));
        sendVerificationLinkMessage.join();
        
        return new VerificationTokenResult(true, verificationToken);
    }

    private Date calculateExpirationDate(int expirationMinutes) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(new Date().getTime());
        calendar.add(Calendar.MINUTE, expirationMinutes);
        return new Date(calendar.getTime().getTime());
    }

    @Override
    @Transactional
    public ResponseEntity<?> verifyUser(String token, Long id) {
        AuthResponse verifyResponse = new AuthResponse();

        // Check if the user is already verified
        boolean checkVerifyUser = authorizeUserVerificationRepository.findUserById(id);
        if (checkVerifyUser) {
            verifyResponse.setMessage("This account has already been verified.");
            verifyResponse.setStatus(true);
            return new ResponseEntity<>(verifyResponse, HttpStatus.OK);
        }

        // Find the verification token
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token);

        if (verificationToken == null) {
            verifyResponse.setMessage("Invalid verification token");
            verifyResponse.setStatus(false);
            return new ResponseEntity<>(verifyResponse, HttpStatus.BAD_REQUEST);
        }

        // Retrieve the user associated with the verification token
        Optional<Users> optionalUser = userRepository.findById(verificationToken.getUserId());
        if (optionalUser.isEmpty()) {
            verifyResponse.setMessage("User not found.");
            verifyResponse.setStatus(false);
            return new ResponseEntity<>(verifyResponse, HttpStatus.BAD_REQUEST);
        }

        Users user = optionalUser.get();

        // Check if the user account is already verified
        if (user.isEnabled()) {
            verifyResponse.setMessage("Hi " + user.getUsername() + ", Your account has already been verified.");
            verifyResponse.setStatus(true);
            return new ResponseEntity<>(verifyResponse, HttpStatus.OK);
        }

        // Check if the token has expired
        Calendar cal = Calendar.getInstance();
        if (verificationToken.getExpirationTime().getTime() - cal.getTime().getTime() < 0) {
            verifyResponse.setMessage("Verification token has expired. Click the resend button to get a new token.");
            verifyResponse.setStatus(false);
            return new ResponseEntity<>(verifyResponse, HttpStatus.CONFLICT);
        }

        // Create wallet and verify user only if wallet creation is successful
        try {
            CompletableFuture<Void> walletCreationFuture = CompletableFuture
                    .runAsync(() -> walletServiceClient.createUserWallet(user.getId()));
            walletCreationFuture.join(); // Wait for wallet creation to complete

            // Update user record status
            Optional<UserRecord> optionalRecord = userRecordRepository.findByUserId(user.getId());
            optionalRecord.ifPresent(userRecord -> {
                userRecord.setStatus(UserStatus.ACTIVE);
                userRecord.setLocked(false);
                userRecord.setBlocked(false);
                userRecordRepository.save(userRecord);
            });

            // Enable user account
            user.setEnabled(true);
            userRepository.save(user);

            // Delete the verification token
            verificationTokenRepository.delete(verificationToken);

            verifyResponse.setMessage("User registration verified successfully.");
            verifyResponse.setStatus(true);
            return new ResponseEntity<>(verifyResponse, HttpStatus.OK);

        } catch (Exception e) {
            // Handle wallet creation failure
            verifyResponse.setMessage(
                    "Failed to create wallet for user. Skipping user verification. Error: " + e.getMessage());
            verifyResponse.setStatus(false);
            return new ResponseEntity<>(verifyResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private Long getNextUserId() {
        List<Users> existingUsers = userRepository.findAll();
        Users newUser = new Users();
        if (existingUsers.isEmpty()) {
            newUser.setId(1001L);
        } else {
            // Find the maximum existing user ID
            Long maxId = existingUsers.stream()
                    .map(Users::getId)
                    .max(Long::compare)
                    .orElse(0L);
            // Set the new user ID
            newUser.setId(maxId + 1);
        }
        return newUser.getId();
    }

    
}
