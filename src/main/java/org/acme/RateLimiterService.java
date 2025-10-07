package org.acme;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.time.Instant;

@ApplicationScoped
public class RateLimiterService {

    private static final int MAX_REQUESTS = 10; // 10 requests
    private static final long WINDOW_SIZE_MS = 60_000; // per minute

    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    public boolean allowRequest(String clientId) {
        TokenBucket bucket = buckets.computeIfAbsent(clientId, k -> new TokenBucket());
        return bucket.tryConsume();
    }

    private static class TokenBucket {
        private int tokens;
        private long lastRefillTimestamp;

        public TokenBucket() {
            this.tokens = MAX_REQUESTS;
            this.lastRefillTimestamp = System.currentTimeMillis();
        }

        public synchronized boolean tryConsume() {
            refill();
            if (tokens > 0) {
                tokens--;
                return true;
            }
            return false;
        }

        private void refill() {
            long now = System.currentTimeMillis();
            long timePassed = now - lastRefillTimestamp;

            if (timePassed >= WINDOW_SIZE_MS) {
                tokens = MAX_REQUESTS;
                lastRefillTimestamp = now;
            }
        }
    }
}
