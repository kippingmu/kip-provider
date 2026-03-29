package xyz.kip.provider.manager.demo.model;

/**
 * Internal manager model for provider demo data.
 */
public class ProviderDemoEchoModel {

    private String provider;
    private String caller;
    private String message;

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getCaller() {
        return caller;
    }

    public void setCaller(String caller) {
        this.caller = caller;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
