package xyz.kip.provider.shutdown;

import jakarta.servlet.AsyncEvent;
import jakarta.servlet.AsyncListener;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Rejects new business traffic after application enters draining state.
 */
public class DrainingTrafficRejectFilter extends OncePerRequestFilter {

    private final ApplicationShutdownStateManager shutdownStateManager;
    private final InFlightWorkTracker inflightWorkTracker;
    private final ShutdownProperties shutdownProperties;

    public DrainingTrafficRejectFilter(ApplicationShutdownStateManager shutdownStateManager,
                                       InFlightWorkTracker inflightWorkTracker,
                                       ShutdownProperties shutdownProperties) {
        this.shutdownStateManager = shutdownStateManager;
        this.inflightWorkTracker = inflightWorkTracker;
        this.shutdownProperties = shutdownProperties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (isActuatorRequest(request)) {
            filterChain.doFilter(request, response);
            return;
        }
        if (!shutdownStateManager.acceptNewWork()) {
            rejectRequest(response);
            return;
        }

        inflightWorkTracker.increment(WorkCategory.HTTP);
        AtomicBoolean completed = new AtomicBoolean();
        try {
            filterChain.doFilter(request, response);
        } finally {
            if (request.isAsyncStarted()) {
                request.getAsyncContext().addListener(new RequestCompletionAsyncListener(completed));
            } else {
                completeRequest(completed);
            }
        }
    }

    private void rejectRequest(HttpServletResponse response) throws IOException {
        response.setStatus(shutdownProperties.getRequestRejectStatus());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader("Connection", "close");
        response.getWriter().write("{\"success\":false,\"message\":\"" + shutdownProperties.getRequestRejectMessage() + "\"}");
    }

    private boolean isActuatorRequest(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        return requestUri != null && requestUri.startsWith("/actuator");
    }

    private void completeRequest(AtomicBoolean completed) {
        if (completed.compareAndSet(false, true)) {
            inflightWorkTracker.decrement(WorkCategory.HTTP);
        }
    }

    private final class RequestCompletionAsyncListener implements AsyncListener {

        private final AtomicBoolean completed;

        private RequestCompletionAsyncListener(AtomicBoolean completed) {
            this.completed = completed;
        }

        @Override
        public void onComplete(AsyncEvent event) {
            completeRequest(completed);
        }

        @Override
        public void onTimeout(AsyncEvent event) {
            completeRequest(completed);
        }

        @Override
        public void onError(AsyncEvent event) {
            completeRequest(completed);
        }

        @Override
        public void onStartAsync(AsyncEvent event) {
            event.getAsyncContext().addListener(this);
        }
    }
}
