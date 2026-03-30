package xyz.kip.provider.service.demo.impl;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import xyz.kip.open.common.base.Result;
import xyz.kip.provider.manager.demo.ProviderDemoManager;
import xyz.kip.provider.manager.demo.model.ProviderDemoEchoModel;
import xyz.kip.provider.service.demo.ProviderDemoService;
import xyz.kip.provider.service.demo.domain.ProviderDemoEchoCommand;
import xyz.kip.provider.service.demo.domain.ProviderDemoEchoDomain;

/**
 * Internal application service for provider demo capabilities.
 * @author xiaoshichuan
 */
@Service
public class ProviderDemoServiceImpl implements ProviderDemoService {

    @Resource
    ProviderDemoManager providerDemoManager;

    @Override
    public Result<ProviderDemoEchoDomain> echo(ProviderDemoEchoCommand command) {
        if (command == null) {
            return Result.failure("request must not be null");
        }
        if (!StringUtils.hasText(command.getMessage())) {
            return Result.failure("message must not be blank");
        }

        Result<ProviderDemoEchoModel> result = providerDemoManager.echo(command.getMessage(), command.getCaller());
        if (!Boolean.TRUE.equals(result.isSuccess())) {
            return Result.failure(result.getMessage());
        }
        return Result.success(toDomain(result.getResult()));
    }

    private ProviderDemoEchoDomain toDomain(ProviderDemoEchoModel model) {
        ProviderDemoEchoDomain domain = new ProviderDemoEchoDomain();
        if (model == null) {
            return domain;
        }
        domain.setProvider(model.getProvider());
        domain.setCaller(model.getCaller());
        domain.setMessage(model.getMessage());
        return domain;
    }
}
