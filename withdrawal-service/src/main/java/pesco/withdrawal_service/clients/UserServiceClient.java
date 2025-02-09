package pesco.withdrawal_service.clients;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import pesco.withdrawal_service.dto.UserDTO;
import pesco.withdrawal_service.enums.BanActions;
import pesco.withdrawal_service.exceptions.UserClientNotFoundException;
import reactor.core.publisher.Mono;

@Service
public class UserServiceClient {

    private final WebClient authServiceWebClient;

    @Autowired
    public UserServiceClient(WebClient authServiceWebClient) {
        this.authServiceWebClient = authServiceWebClient;
    }

    public UserDTO getUserById(Long id, String token) {
        return this.authServiceWebClient.get()
                .uri("/api/v1/user/by/public/userId/{userId}", id)
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(errorMessage -> {
                                    if (clientResponse.statusCode().is4xxClientError()) {
                                        String details = extractDetailsFromError(errorMessage);
                                        return Mono.error(
                                                new UserClientNotFoundException("User not found: " + details, errorMessage));
                                    }
                                    return Mono.error(new RuntimeException("Server error: " + errorMessage));
                                }))
                .bodyToMono(String.class)
                .flatMap(response -> {
                    // Convert the raw response to UserDTO
                    UserDTO userDTO = convertToUserDTO(response);
                    return Mono.just(userDTO);
                })
                .switchIfEmpty(Mono.error(new UserClientNotFoundException("User not found: No data returned", token)))
                .block();

    }

    public UserDTO authenticateUser(String username, String token) {
        return this.authServiceWebClient.get()
                .uri("/api/v1/user/by/username/{username}", username)
                .headers(headers -> headers.setBearerAuth(token))
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(errorMessage -> {
                                    if (clientResponse.statusCode().is4xxClientError()) {
                                        String details = extractDetailsFromError(errorMessage);
                                        return Mono.error(new UserClientNotFoundException("User not found", details));
                                    }
                                    return Mono.error(new RuntimeException("Server error"));
                                }))
                .bodyToMono(UserDTO.class)
                .block();
    }

    public UserDTO getUserByUsername(String username) {
        return this.authServiceWebClient.get()
                .uri("/api/v1/user/by/public/username/{username}", username)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(errorMessage -> {
                                    if (clientResponse.statusCode().is4xxClientError()) {
                                        String details = extractDetailsFromError(errorMessage);
                                        return Mono.error(new UserClientNotFoundException("User not found", details));
                                    }
                                    return Mono.error(new RuntimeException("Server error"));
                                }))
                .bodyToMono(UserDTO.class)
                .block();
    }

    private String extractDetailsFromError(String errorMessage) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(errorMessage);
            return rootNode.path("message").asText();
        } catch (JsonProcessingException e) {
            return "No details available";
        }
    }

    public UserDTO fetchPublicUserById(Long userId) {
        return this.authServiceWebClient.get()
                .uri("/api/v1/user/by/public/userId/{userId}", userId)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(errorMessage -> {
                                    if (clientResponse.statusCode().is4xxClientError()) {
                                        String details = extractDetailsFromError(errorMessage);
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

    public void updateUserAccountStatus(Long id, BanActions suspiciousActivity, String token) {
        try {
            // Send the PUT request
            this.authServiceWebClient.put()
                    .uri("/api/v1/user/{id}/status", id) 
                    .header("Authorization", "Bearer " + token) 
                    .bodyValue(suspiciousActivity) 
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .flatMap(errorMessage -> {
                                        if (clientResponse.statusCode().is4xxClientError()) {
                                            String details = extractDetailsFromError(errorMessage);
                                            return Mono
                                                    .error(new UserClientNotFoundException("User not found", details));
                                        }
                                        return Mono.error(new RuntimeException("Server error"));
                                    }))
                    .toBodilessEntity()
                    .block(); 
        } catch (Exception ex) {
            System.err.println("Error updating user account status: " + ex.getMessage());
        }
    }

    public void blockUserAccount(Long id, String token) {
        this.authServiceWebClient.put()
        .uri("/api/v1/user/block/userId/{id}", id)
        .header("Authorization", "Bearer " + token)
        .bodyValue(Map.of("blocked", true)) 
        .retrieve()
        .onStatus(HttpStatusCode::isError, response -> response.bodyToMono(String.class)
                .flatMap(errorBody -> {
                    System.err.println("Error Response: " + errorBody);
                    return Mono.error(new RuntimeException("User account blocking failed: " + errorBody));
                }))
        .bodyToMono(String.class)
        .block();
    }

}
