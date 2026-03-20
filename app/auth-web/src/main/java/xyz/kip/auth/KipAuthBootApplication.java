package xyz.kip.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author xiaoshichuan
 * @version 2023-07-06 17:24
 */
@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages= {"xyz.kip"}, exclude = UserDetailsServiceAutoConfiguration.class)
public class KipAuthBootApplication {
    /**
     * 主启动方法
     * @param args args
     */
    public static void main(String[] args) {
        SpringApplication.run(KipAuthBootApplication.class, args);
    }
}
