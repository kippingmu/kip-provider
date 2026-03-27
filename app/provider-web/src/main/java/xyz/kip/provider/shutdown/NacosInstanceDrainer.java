package xyz.kip.provider.shutdown;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;
import org.springframework.stereotype.Component;

/**
 * Deregisters the current application instance from the service registry.
 */
@Component
public class NacosInstanceDrainer {

    private static final Logger log = LoggerFactory.getLogger(NacosInstanceDrainer.class);

    private final ObjectProvider<ServiceRegistry<Registration>> serviceRegistryProvider;
    private final ObjectProvider<Registration> registrationProvider;

    public NacosInstanceDrainer(ObjectProvider<ServiceRegistry<Registration>> serviceRegistryProvider,
                                ObjectProvider<Registration> registrationProvider) {
        this.serviceRegistryProvider = serviceRegistryProvider;
        this.registrationProvider = registrationProvider;
    }

    public boolean deregisterCurrentInstance() {
        ServiceRegistry<Registration> serviceRegistry = serviceRegistryProvider.getIfAvailable();
        Registration registration = registrationProvider.getIfAvailable();
        if (serviceRegistry == null || registration == null) {
            log.info("skip registry deregistration because service registry or registration bean is unavailable");
            return false;
        }
        log.info("start registry deregistration, serviceId={}, host={}, port={}",
                registration.getServiceId(), registration.getHost(), registration.getPort());
        serviceRegistry.deregister(registration);
        log.info("registry deregistration completed, serviceId={}, host={}, port={}",
                registration.getServiceId(), registration.getHost(), registration.getPort());
        return true;
    }
}
