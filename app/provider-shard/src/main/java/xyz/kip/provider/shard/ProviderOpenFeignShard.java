package xyz.kip.provider.shard;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import xyz.kip.open.common.base.Result;


/**
 * @author xiaoshichuan
 */
public interface ProviderOpenFeignShard {

    @PostMapping(value = "/provider/demo/echo", consumes = MediaType.APPLICATION_JSON_VALUE)
    Result<ProviderDemoEchoRespDTO> echo(@RequestBody ProviderDemoEchoReqDTO req);
}
