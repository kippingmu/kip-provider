package xyz.kip.provider.service.demo.domain;

/**
 * Internal command object for provider demo requests.
 */
public class ProviderDemoEchoCommand {

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
