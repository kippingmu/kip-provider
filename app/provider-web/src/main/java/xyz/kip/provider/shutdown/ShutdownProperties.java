package xyz.kip.provider.shutdown;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Configurable graceful shutdown properties.
 */
@ConfigurationProperties(prefix = "kip.shutdown")
public class ShutdownProperties {

    private Duration propagationWait = Duration.ofSeconds(5);
    private Duration inFlightWait = Duration.ofSeconds(30);
    private Duration forceStopWait = Duration.ofSeconds(5);
    private int requestRejectStatus = 503;
    private String requestRejectMessage = "service is draining";

    public Duration getPropagationWait() {
        return propagationWait;
    }

    public void setPropagationWait(Duration propagationWait) {
        this.propagationWait = propagationWait;
    }

    public Duration getInFlightWait() {
        return inFlightWait;
    }

    public void setInFlightWait(Duration inFlightWait) {
        this.inFlightWait = inFlightWait;
    }

    public Duration getForceStopWait() {
        return forceStopWait;
    }

    public void setForceStopWait(Duration forceStopWait) {
        this.forceStopWait = forceStopWait;
    }

    public int getRequestRejectStatus() {
        return requestRejectStatus;
    }

    public void setRequestRejectStatus(int requestRejectStatus) {
        this.requestRejectStatus = requestRejectStatus;
    }

    public String getRequestRejectMessage() {
        return requestRejectMessage;
    }

    public void setRequestRejectMessage(String requestRejectMessage) {
        this.requestRejectMessage = requestRejectMessage;
    }
}
