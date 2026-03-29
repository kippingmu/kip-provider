package xyz.kip.provider.service.demo.domain;

/**
 * Internal domain object returned by provider demo service.
 */
public class ProviderDemoEchoDomain {

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
