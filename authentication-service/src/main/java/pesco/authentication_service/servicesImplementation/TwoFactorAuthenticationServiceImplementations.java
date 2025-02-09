package pesco.authentication_service.servicesImplementation;

import java.util.Optional;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import pesco.authentication_service.exceptions.Error;
import pesco.authentication_service.models.TwoFactorAuthentication;
import pesco.authentication_service.models.Users;
import pesco.authentication_service.payloads.OTPRequest;
import pesco.authentication_service.repositories.TwoFactorOTPRepository;
import pesco.authentication_service.repositories.UsersRepository;
import pesco.authentication_service.services.JwtService;
import pesco.authentication_service.services.TwoFactorAuthenticationService;
;

@Service
public class TwoFactorAuthenticationServiceImplementations implements TwoFactorAuthenticationService {

    private final TwoFactorOTPRepository twoFactorOTPRepository;
    private static final int EXPIRATION_MINUTES = 15;
    private Date expirationTime;
    private final UsersRepository userRepository;
    private final JwtService jwtService;

    public TwoFactorAuthenticationServiceImplementations(TwoFactorOTPRepository twoFactorOTPRepository,
            UsersRepository userRepository, JwtService jwtService) {
        this.twoFactorOTPRepository = twoFactorOTPRepository;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @Override
    public TwoFactorAuthentication createTwoFactorOtp(Users authUser, String otp, String jwtToken) {
        expirationTime = calculateExpirationDate(EXPIRATION_MINUTES);
        TwoFactorAuthentication twoFactorOTP = TwoFactorAuthentication.builder()
                .otp(otp)
                .token(jwtToken)
                .expirationTime(expirationTime)
                .userId(authUser.getId())
                .build();
        return twoFactorOTPRepository.save(twoFactorOTP);
    }

    @Override
    public TwoFactorAuthentication findByUser(Long userid) {
        return twoFactorOTPRepository.findByUserId(userid);
    }

    @Override
    public TwoFactorAuthentication findById(Long id) {
        Optional<TwoFactorAuthentication> opt = twoFactorOTPRepository.findById(id);
        return opt.orElse(null);
    }

    @Override
    public boolean verifyTwoFactorOtp(TwoFactorAuthentication twoFactorOTP, String otp) {
        return twoFactorOTP.getOtp().equals(otp);
    }

    @Override
    public void deleteTwoFactorOtp(TwoFactorAuthentication twoFactorOTP) {
        twoFactorOTPRepository.delete(twoFactorOTP);
    }

    @Override
    public ResponseEntity<?> findByToken(String token) {
        Optional<TwoFactorAuthentication> checkExists = twoFactorOTPRepository.findByJwt(token);
        Map<String, Object> response = new HashMap<>();

        if (checkExists.isPresent()) {
            TwoFactorAuthentication twoFactorOTP = checkExists.get();
            // Check if the token has expired
            Date currentDate = new Date();
            if (currentDate.after(twoFactorOTP.getExpirationTime())) {
                // Token has expired
                response.put("status", false);
                response.put("message", "Token has expired");
            } else {
                Optional<Users> user = userRepository.findById(twoFactorOTP.getUserId());
                // Check if user is present
                if (user.isPresent()) {
                    response.put("status", true);
                    response.put("email", user.get().getEmail());
                } else {
                    response.put("status", false);
                    response.put("email", "");
                }
            }
        } else {
            response.put("status", false);
            response.put("message", "Token not found");
        }

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<?> verifyUserTwoFactorOtp(OTPRequest reqOtpPayload) {
        Optional<TwoFactorAuthentication> verifyExistingOTP = twoFactorOTPRepository.findByOTP(reqOtpPayload.getOtp());
        Map<String, Object> response = new HashMap<>();

        Date currentDate = new Date();

        if (verifyExistingOTP.isPresent()) {

            // Check if the OTP has expired
            if (currentDate.after(verifyExistingOTP.get().getExpirationTime())) {
                // Token has expired
                response.put("status", false);
                response.put("success", false);
                response.put("message", "Token has expired");

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // Proceed with OTP verification
            Optional<Users> userInfo = userRepository.findById(verifyExistingOTP.get().getUserId());

            if (userInfo.isPresent()) {
                var userjwtInfo = userRepository.findByUsername(userInfo.get().getUsername())
                        .orElseThrow(() -> new AuthenticationServiceException("User not found"));

                // Generate JWT token
                var jwtToken = jwtService.generateToken((UserDetails) userjwtInfo);

                response.put("jwt", jwtToken);
                response.put("userId", userInfo.get().getId());
                response.put("status", true);
                response.put("success", true);
                response.put("session", null);
                response.put("twoFactorAuthEnabled", userInfo.get().isTwoFactorAuth());
                response.put("username", userInfo.get().getUsername());

                // Delete the OTP entry after successful verification
                twoFactorOTPRepository.delete(verifyExistingOTP.get());

                return ResponseEntity.ok(response);
            } else {
                response.put("jwt", null);
                response.put("userId", null);
                response.put("username", null);
                response.put("success", false);
                response.put("message", "No token found");

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
        } else {
            response.put("jwt", null);
            response.put("userId", null);
            response.put("username", null);
            response.put("success", false);
            response.put("message", "Invalid OTP Provided. Please check your email and try again.");

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @Override
    public ResponseEntity<?> enableTwoFactorKey(Boolean enable2fa, Authentication authentication) {
        String username = authentication.getName();
        Optional<Users> optionalUser = userRepository.findByUsername(username);
        if (!optionalUser.isPresent()) {
            return Error.createResponse(
                    "UNAUTHORIZE ACCESS", HttpStatus.FORBIDDEN,
                    "You dont have access to the endpoints");
        }
        Users user = optionalUser.get();
        user.setTwoFactorAuth(enable2fa);
        userRepository.save(user);
        return ResponseEntity.ok().body("Two-Factor Authentication updated successfully");
    }

    private Date calculateExpirationDate(int expirationMinutes) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(new Date().getTime());
        calendar.add(Calendar.MINUTE, expirationMinutes);
        return new Date(calendar.getTime().getTime());
    }

}
