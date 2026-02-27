package com.programming.techie.apigateway;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.programming.techie.apigateway.AuthValidationFilter.UserInfoDetails;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import reactor.core.publisher.Mono;

@Component
public class AuthValidationFilter implements GlobalFilter, Ordered {

    private final WebClient.Builder webClientBuilder;
    private final ObservationRegistry observationRegistry;
    private final ObjectMapper objectMapper = new ObjectMapper()
    .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    private String authValidateUrl = "http://auth-service/api/v1/user/userinfovalidate";

    public AuthValidationFilter(WebClient.Builder webClientBuilder, ObservationRegistry observationRegistry) {
        this.webClientBuilder = webClientBuilder;
        this.observationRegistry = observationRegistry;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        System.out.println("Incoming request: " + request.getMethod() + " " + request.getURI());

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return chain.filter(exchange);
        }

        String token = authHeader.substring(7); // Extract the token

        Observation authObservation = Observation.createNotStarted("auth-service-lookup", this.observationRegistry);
        authObservation.lowCardinalityKeyValue("call", "auth-service");
        return authObservation.observe(() -> webClientBuilder.build()
                .get()
                .uri(authValidateUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .onStatus(status -> status.isError(), clientResponse ->
                        clientResponse.createException().flatMap(Mono::error)
                )
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<UserInfoDetails>>() {})
                .flatMap(user -> {
    if (user != null) {
        exchange.getAttributes().put("userDetails", user);
        System.out.println("User details set: " + user);
        try {
            String userJson = objectMapper.writeValueAsString(user);
            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header("X-User-Info", userJson)
                .build();
            ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();
            return chain.filter(mutatedExchange);
        } catch (Exception ex) {
            exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            return exchange.getResponse().setComplete();
        }
    } else {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
})
                .onErrorResume(e -> {
                   if (e instanceof org.springframework.web.reactive.function.client.WebClientResponseException ex) {
                        exchange.getResponse().setStatusCode(ex.getStatusCode());
                        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(ex.getResponseBodyAsByteArray());
                        return exchange.getResponse().writeWith(Mono.just(buffer));
                    } else {
                        exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
                        return exchange.getResponse().setComplete();
                    }
                }));
    }

    @Override
    public int getOrder() {
        return -1; // High precedence
    }

    // DTO for user info
    public static class UserInfoDetails {
        private String username;
        private String mobileNumber;
        private Object authorities;
        private boolean accountNonExpired;
        private boolean accountNonLocked;
        private boolean credentialsNonExpired;
        private boolean enabled;

        public UserInfoDetails(String username, String mobileNumber, Object authorities,
                boolean accountNonExpired, boolean accountNonLocked,
                boolean credentialsNonExpired, boolean enabled) {
            this.username = username;
            this.mobileNumber = mobileNumber;
            this.authorities = authorities;
            this.accountNonExpired = accountNonExpired;
            this.accountNonLocked = accountNonLocked;
            this.credentialsNonExpired = credentialsNonExpired;
            this.enabled = enabled;
        }

        // Getters and setters
        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getMobileNumber() {
            return mobileNumber;
        }

        public void setMobileNumber(String mobileNumber) {
            this.mobileNumber = mobileNumber;
        }

        public Object getAuthorities() {
            return authorities;
        }

        public void setAuthorities(Object authorities) {
            this.authorities = authorities;
        }

        public boolean isAccountNonExpired() {
            return accountNonExpired;
        }

        public void setAccountNonExpired(boolean accountNonExpired) {
            this.accountNonExpired = accountNonExpired;
        }

        public boolean isAccountNonLocked() {
            return accountNonLocked;
        }

        public void setAccountNonLocked(boolean accountNonLocked) {
            this.accountNonLocked = accountNonLocked;
        }

        public boolean isCredentialsNonExpired() {
            return credentialsNonExpired;
        }

        public void setCredentialsNonExpired(boolean credentialsNonExpired) {
            this.credentialsNonExpired = credentialsNonExpired;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        @Override
        public String toString() {
            return "UserInfoDetails{" +
                    "username='" + username + '\'' +
                    ", mobileNumber='" + mobileNumber + '\'' +
                    ", authorities=" + authorities +
                    ", accountNonExpired=" + accountNonExpired +
                    ", accountNonLocked=" + accountNonLocked +
                    ", credentialsNonExpired=" + credentialsNonExpired +
                    ", enabled=" + enabled +
                    '}';
        }
    }
}

// final String authHeader = request.getHeader("Authorization");

// if (authHeader == null || !authHeader.startsWith("Bearer ")) {
// filterChain.doFilter(request, response);
// return;
// }

// try {
// final String jwt = authHeader.substring(7);
// final String username = jwtService.extractUsername(jwt);

// if (username != null &&
// SecurityContextHolder.getContext().getAuthentication() == null) {

// // Validate token type (should be access token)
// if (!jwtService.isAccessToken(jwt)) {
// log.warn("Invalid token type used for authentication");
// filterChain.doFilter(request, response);
// return;
// }

// // Validate session exists and is active
// if (!sessionService.findByAccessToken(jwt).isPresent()) {
// log.warn("No active session found for token");
// filterChain.doFilter(request, response);
// return;
// }

// UserDetails userDetails = userDetailsService.loadUserByUsername(username);

// if (jwtService.isValidToken(jwt, userDetails)) {
// UsernamePasswordAuthenticationToken authToken = new
// UsernamePasswordAuthenticationToken(
// userDetails,
// null,
// userDetails.getAuthorities());
// authToken.setDetails(new
// WebAuthenticationDetailsSource().buildDetails(request));
// SecurityContextHolder.getContext().setAuthentication(authToken);
// }
// }
// } catch (Exception e) {
// log.error("Cannot set user authentication: {}", e.getMessage());
// }

// filterChain.doFilter(request, response);