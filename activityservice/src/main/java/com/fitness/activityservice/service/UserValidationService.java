package com.fitness.activityservice.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class UserValidationService {

    private final WebClient userServiceWebClient;
    private static final Logger logger = LoggerFactory.getLogger(UserValidationService.class);

    public boolean validateUser(String userId) {
        logger.info("Validating user with ID: {}", userId);

        try {
            Boolean result = userServiceWebClient.get()
                    .uri("/api/users/{userId}/validate", userId)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .timeout(Duration.ofSeconds(3))
                    .block();

            return Boolean.TRUE.equals(result);

        } catch (WebClientResponseException e) {
            // Only return false if the User Service explicitly says "404 Not Found"
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                logger.warn("User Service returned 404 for userId: {}", userId);
                return false;
            }
            // For other errors (500, 400), throw the exception so you see it
            logger.error("User Service error: {}", e.getStatusCode());
            throw new RuntimeException("Error validating user: " + e.getMessage());

        } catch (Exception e) {
            // This catches the "No servers available" load balancer error
            logger.error("System error calling User Service", e);
            throw new RuntimeException("Service unavailable: " + e.getMessage());
        }
    }
}