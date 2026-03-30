package xyz.kip.provider.facade.impl;

import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestController;
import xyz.kip.open.common.base.Result;
import xyz.kip.provider.service.demo.ProviderDemoService;
import xyz.kip.provider.service.demo.domain.ProviderDemoEchoCommand;
import xyz.kip.provider.service.demo.domain.ProviderDemoEchoDomain;
import xyz.kip.provider.shard.ProviderDemoEchoReqDTO;
import xyz.kip.provider.shard.ProviderDemoEchoRespDTO;
import xyz.kip.provider.shard.ProviderOpenFeignShard;

/**
 * @author xiaoshichuan
 * @version 2026-03-29 18:28, Sun
 */
@RestController
public class OpenFeignFacadeImpl implements ProviderOpenFeignShard {

    private static final Logger log = LoggerFactory.getLogger(OpenFeignFacadeImpl.class);

    @Resource
   ProviderDemoService providerDemoService;

    @Override
    public Result<ProviderDemoEchoRespDTO> echo(ProviderDemoEchoReqDTO req) {
        log.info("OpenFeignFacadeImpl received internal OpenFeign request");
        Result<ProviderDemoEchoDomain> result = providerDemoService.echo(toCommand(req));
        if (!Boolean.TRUE.equals(result.isSuccess())) {
            return Result.failure(result.getMessage());
        }
        return Result.success(toRespDTO(result.getResult()));
    }

    private ProviderDemoEchoCommand toCommand(ProviderDemoEchoReqDTO req) {
        ProviderDemoEchoCommand command = new ProviderDemoEchoCommand();
        if (req == null) {
            return command;
        }
        command.setCaller(req.getCaller());
        command.setMessage(req.getMessage());
        return command;
    }

    private ProviderDemoEchoRespDTO toRespDTO(ProviderDemoEchoDomain domain) {
        ProviderDemoEchoRespDTO respDTO = new ProviderDemoEchoRespDTO();
        if (domain == null) {
            return respDTO;
        }
        respDTO.setMessage(domain.getMessage());
        respDTO.setProvider(domain.getProvider());
        respDTO.setCaller(domain.getCaller());
        return respDTO;
    }
}
