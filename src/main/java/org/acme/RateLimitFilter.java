package org.acme;

import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Provider
public class RateLimitFilter implements ContainerRequestFilter {

    @Inject
    RateLimiterService rateLimiterService;

    @ConfigProperty(name = "rate.limit.requests", defaultValue = "10")
    int maxRequests;

    @ConfigProperty(name = "rate.limit.window.seconds", defaultValue = "60")
    int windowSeconds;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String clientIp = getClientIp(requestContext);

        if (!rateLimiterService.allowRequest(clientIp)) {
            // Rate limit exceeded
            requestContext.abortWith(
                Response.status(429)
                    .entity("{\"error\": \"Too many requests. Please try again later.\"}")
                    .header("X-RateLimit-Limit", maxRequests)
                    .header("X-RateLimit-Remaining", 0)
                    .header("Retry-After", windowSeconds)
                    .build()
            );
        }
    }

    private String getClientIp(ContainerRequestContext requestContext) {
        String xForwardedFor = requestContext.getHeaderString("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = requestContext.getHeaderString("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        // Fallback to a default identifier if IP is not available
        return "unknown";
    }
}
