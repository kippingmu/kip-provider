package xyz.kip.provider.shard;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import xyz.kip.open.common.base.Result;

/**
 * Shared internal API contract for services that call kip-provider.
 * @author xiaoshichuan
 */
@FeignClient(
        contextId = "kipProviderClient",
        name = "kip-provider"
)
@RequestMapping("/provider/demo")
public interface ProviderOpenFeignShard {

    @PostMapping(value = "/echo", consumes = MediaType.APPLICATION_JSON_VALUE)
    Result<ProviderDemoEchoRespDTO> echo(@RequestBody ProviderDemoEchoReqDTO req);
}
