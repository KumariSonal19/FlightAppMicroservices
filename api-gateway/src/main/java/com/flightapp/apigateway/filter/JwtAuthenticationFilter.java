package com.flightapp.apigateway.filter;

import com.flightapp.apigateway.util.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.util.List;

@Component
@Slf4j
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    @Autowired
    private JwtUtils jwtUtils;
    private static final String FLIGHT_SEARCH_PATH = "/api/flight/search";

    public JwtAuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getURI().getPath();
            HttpMethod method = exchange.getRequest().getMethod();

            log.debug("Applying JWT authentication filter to {} {}", method, path);
            if (isPublicRoute(path)) {
                log.debug("Public route detected, skipping JWT validation");
                return chain.filter(exchange);
            }

            String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Missing or invalid Authorization header");
                return this.unauthorized(exchange);
            }

            String token = authHeader.substring(7);

            if (!jwtUtils.validateJwtToken(token)) {
                log.warn("Invalid JWT token");
                return this.unauthorized(exchange);
            }

            List<String> roles = jwtUtils.getRolesFromJwtToken(token);
            log.debug("Roles from token: {}", roles);

            if (isFlightInventoryModification(path, method)) {
                if (!roles.contains("ROLE_ADMIN") && !roles.contains("ROLE_MODERATOR")) {
                    log.warn("User lacks required role to modify flight inventory");
                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                    return exchange.getResponse().setComplete();
                }
            }

            return chain.filter(exchange);
        };
    }

    private boolean isPublicRoute(String path) {
        return path.startsWith("/api/auth")
                || path.startsWith("/actuator")
                || path.startsWith(FLIGHT_SEARCH_PATH);  
    }

    private boolean isFlightInventoryModification(String path, HttpMethod method) {
        if (path.startsWith("/api/flight") && !path.startsWith(FLIGHT_SEARCH_PATH)) {
            return HttpMethod.POST.equals(method)
                    || HttpMethod.PUT.equals(method)
                    || HttpMethod.DELETE.equals(method);
        }
        return false;
    }

    private reactor.core.publisher.Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    public static class Config {
      
    }
}
