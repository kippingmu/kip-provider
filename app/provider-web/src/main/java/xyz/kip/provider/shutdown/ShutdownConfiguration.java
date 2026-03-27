package xyz.kip.provider.shutdown;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * Graceful shutdown bean configuration.
 */
@Configuration
@EnableConfigurationProperties(ShutdownProperties.class)
public class ShutdownConfiguration {

    @Bean
    public ApplicationShutdownStateManager applicationShutdownStateManager() {
        return new ApplicationShutdownStateManager();
    }

    @Bean
    public InFlightWorkTracker inFlightWorkTracker() {
        return new InFlightWorkTracker();
    }

    @Bean
    public DrainingTrafficRejectFilter drainingTrafficRejectFilter(ApplicationShutdownStateManager shutdownStateManager,
                                                                   InFlightWorkTracker inflightWorkTracker,
                                                                   ShutdownProperties shutdownProperties) {
        return new DrainingTrafficRejectFilter(shutdownStateManager, inflightWorkTracker, shutdownProperties);
    }

    @Bean
    public FilterRegistrationBean<DrainingTrafficRejectFilter> drainingTrafficRejectFilterRegistration(
            DrainingTrafficRejectFilter filter) {
        FilterRegistrationBean<DrainingTrafficRejectFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(filter);
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registrationBean.addUrlPatterns("/*");
        return registrationBean;
    }
}
