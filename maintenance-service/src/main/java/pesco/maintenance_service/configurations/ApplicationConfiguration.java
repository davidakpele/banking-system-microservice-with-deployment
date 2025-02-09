package pesco.maintenance_service.configurations;

import lombok.RequiredArgsConstructor;
import pesco.maintenance_service.clients.UserServiceClient;
import pesco.maintenance_service.dtos.UserDTO;
import pesco.maintenance_service.dtos.UserRecordDTO;
import pesco.maintenance_service.utils.TokenExtractor;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import jakarta.servlet.http.HttpServletRequest;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfiguration {

    private final UserServiceClient userServiceClient;
    private final TokenExtractor tokenExtractor;
    private final HttpServletRequest request;
    
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            try {
                String token = tokenExtractor.extractToken(request);
                UserDTO userDTO = userServiceClient.getUserByUsername(username, token);

                if (userDTO != null) {
                    // Map user roles if necessary, here defaulting to ROLE_USER
                    List<SimpleGrantedAuthority> authorities = userDTO.getRecords().stream()
                            .map(record -> new SimpleGrantedAuthority("ROLE_USER"))
                            .toList();
                    return new org.springframework.security.core.userdetails.User(
                            userDTO.getUsername(),
                            "", 
                            userDTO.isEnabled(),
                            true, 
                            true, 
                            !isAccountLocked(userDTO), 
                            authorities
                    );
                } else {
                    throw new UsernameNotFoundException("User not found: " + username);
                }
            } catch (Exception e) {
                // Log the error and throw an exception to prevent unauthorized access
                throw new UsernameNotFoundException("Unable to fetch user details for: " + username, e);
            }
        };
    }

    // Helper method to determine if the account is locked
    private boolean isAccountLocked(UserDTO userDTO) {
        return userDTO.getRecords().stream().anyMatch(UserRecordDTO::isLocked);
    }


    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
