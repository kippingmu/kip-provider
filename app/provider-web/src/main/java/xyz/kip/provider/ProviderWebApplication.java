package xyz.kip.provider;

import com.alibaba.nacos.client.config.utils.SnapShotSwitch;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author xiaoshichuan
 * @version 2026-03-27 21:59, Fri
 */
@SpringBootApplication
@ComponentScan(basePackages = "xyz.kip")
@MapperScan("xyz.kip.provider.dal.mapper")
public class ProviderWebApplication {

    private static final String DEV_PROFILE = "dev";
    private static final String DEFAULT_PROFILE = DEV_PROFILE;

    public static void main(String[] args) {
        disableNacosSnapshotFallbackForDev(args);
        SpringApplication.run(ProviderWebApplication.class, args);
    }

    private static void disableNacosSnapshotFallbackForDev(String[] args) {
        if (!resolveActiveProfiles(args).contains(DEV_PROFILE)) {
            return;
        }
        SnapShotSwitch.setIsSnapShot(Boolean.FALSE);
    }

    private static Set<String> resolveActiveProfiles(String[] args) {
        Set<String> profiles = new LinkedHashSet<>();
        addProfiles(profiles, System.getProperty("spring.profiles.active"));
        addProfiles(profiles, System.getenv("SPRING_PROFILES_ACTIVE"));
        if (args != null) {
            for (String arg : args) {
                if (arg != null && arg.startsWith("--spring.profiles.active=")) {
                    addProfiles(profiles, arg.substring("--spring.profiles.active=".length()));
                }
            }
        }
        if (profiles.isEmpty()) {
            profiles.add(DEFAULT_PROFILE);
        }
        return profiles;
    }

    private static void addProfiles(Set<String> profiles, String rawProfiles) {
        if (rawProfiles == null || rawProfiles.isBlank()) {
            return;
        }
        Arrays.stream(rawProfiles.split(","))
                .map(String::trim)
                .filter(profile -> !profile.isEmpty())
                .forEach(profiles::add);
    }
}
