package org.acme;

import jakarta.annotation.Priority;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Provider
@Priority(2000)
public class TimeoutInterceptor implements ContainerRequestFilter, ContainerResponseFilter {

    @ConfigProperty(name = "quarkus.rest.timeout", defaultValue = "30")
    long timeoutSeconds;

    private final Map<String, Long> requestTimes = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String requestId = System.currentTimeMillis() + "-" + Thread.currentThread().getId();
        requestContext.setProperty("requestId", requestId);
        requestTimes.put(requestId, System.currentTimeMillis());

        // Schedule timeout check
        scheduler.schedule(() -> {
            Long startTime = requestTimes.get(requestId);
            if (startTime != null) {
                long elapsed = System.currentTimeMillis() - startTime;
                if (elapsed >= timeoutSeconds * 1000) {
                    requestContext.abortWith(
                        Response.status(504)
                            .entity("{\"error\": \"Request timeout\"}")
                            .build()
                    );
                }
            }
        }, timeoutSeconds, TimeUnit.SECONDS);
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        String requestId = (String) requestContext.getProperty("requestId");
        if (requestId != null) {
            requestTimes.remove(requestId);
        }
    }
}
