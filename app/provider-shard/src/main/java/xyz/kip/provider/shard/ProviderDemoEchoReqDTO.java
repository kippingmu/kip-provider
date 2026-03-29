package xyz.kip.provider.shard;

/**
 * Demo request for internal calls from kip-app to kip-provider.
 */
public class ProviderDemoEchoReqDTO {

    private String message;
    private String caller;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCaller() {
        return caller;
    }

    public void setCaller(String caller) {
        this.caller = caller;
    }
}
