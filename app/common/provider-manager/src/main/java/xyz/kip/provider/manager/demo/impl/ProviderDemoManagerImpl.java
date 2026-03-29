package xyz.kip.provider.manager.demo.impl;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import xyz.kip.open.common.base.Result;
import xyz.kip.provider.manager.demo.ProviderDemoManager;
import xyz.kip.provider.manager.demo.model.ProviderDemoEchoModel;

/**
 * Internal manager implementation for provider demo capabilities.
 */
@Component
public class ProviderDemoManagerImpl implements ProviderDemoManager {

    @Override
    public Result<ProviderDemoEchoModel> echo(String message, String caller) {
        if (!StringUtils.hasText(message)) {
            return Result.failure("message must not be blank");
        }

        ProviderDemoEchoModel model = new ProviderDemoEchoModel();
        model.setProvider("kip-provider");
        model.setCaller(StringUtils.hasText(caller) ? caller : "unknown");
        model.setMessage("provider received: " + message);
        return Result.success(model);
    }
}
