package xyz.kip.provider.service.demo;

import xyz.kip.open.common.base.Result;
import xyz.kip.provider.service.demo.domain.ProviderDemoEchoCommand;
import xyz.kip.provider.service.demo.domain.ProviderDemoEchoDomain;

/**
 * Internal application service for provider demo capabilities.
 */
public interface ProviderDemoService {

    Result<ProviderDemoEchoDomain> echo(ProviderDemoEchoCommand command);
}
