package pesco.revenue_service.clients;

import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import pesco.revenue_service.dto.UserDTO;
import pesco.revenue_service.exceptions.Extraction;
import pesco.revenue_service.exceptions.UserClientNotFoundException;
import reactor.core.publisher.Mono;

@Service
public class UserServiceClient {

    private final WebClient  authServiceWebClient;
    private final Extraction extraction;

    public UserServiceClient(WebClient authServiceWebClient, Extraction extraction) {
        this.authServiceWebClient = authServiceWebClient;
        this.extraction = extraction;
    }
    
    public UserDTO getUserByUsername(String username, String token) {
        return this.authServiceWebClient.get()
                .uri("/api/v1/user/by/username/{username}", username)
                .headers(headers -> headers.setBearerAuth(token))
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(errorMessage -> {
                                    if (clientResponse.statusCode().is4xxClientError()) {
                                        String details = extraction.extractDetailsFromError(errorMessage);
                                        return Mono.error(new UserClientNotFoundException("User not found", details));
                                    }
                                    return Mono.error(new RuntimeException("Server error"));
                                }))
                .bodyToMono(UserDTO.class)
                .block();
    }

  

    public UserDTO convertToUserDTO(String response) {
        try {
            // Create an ObjectMapper instance
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());

            // Convert the raw JSON response to a UserDTO object
            return objectMapper.readValue(response, UserDTO.class);
        } catch (Exception e) {
            // Log the error details for better debugging
            System.err.println("Error converting response to UserDTO: " + e.getMessage());
            e.printStackTrace();

            // Rethrow the exception with additional context
            throw new RuntimeException("Error converting response to UserDTO", e);
        }
    }

    public List<UserDTO> findAllUserId(Long userId) {
        try {
            return this.authServiceWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/auth/users/all")
                            .queryParam("userId", userId)
                            .build())
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .flatMap(errorMessage -> {
                                        if (clientResponse.statusCode().is4xxClientError()) {
                                            String details = extraction.extractDetailsFromError(errorMessage);
                                            return Mono
                                                    .error(new UserClientNotFoundException("User not found", details));
                                        }
                                        return Mono
                                                .error(new RuntimeException("Server error while fetching user data"));
                                    }))
                    .bodyToFlux(UserDTO.class) // Expecting a list response
                    .collectList()
                    .block();

        } catch (Exception ex) {
            System.err.println("Error fetching user details: " + ex.getMessage());
            return Collections.emptyList(); 
        }
    }

}
