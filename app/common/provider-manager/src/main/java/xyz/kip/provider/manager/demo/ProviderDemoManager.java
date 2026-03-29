package xyz.kip.provider.manager.demo;

import xyz.kip.open.common.base.Result;
import xyz.kip.provider.manager.demo.model.ProviderDemoEchoModel;

/**
 * Internal manager for provider demo capabilities.
 */
public interface ProviderDemoManager {

    Result<ProviderDemoEchoModel> echo(String message, String caller);
}
