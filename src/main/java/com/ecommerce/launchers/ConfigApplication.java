package com.ecommerce.launchers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootApplication(
        scanBasePackages = "com.ecommerce.univ.config",
        excludeName = {
                "org.springframework.cloud.gateway.server.mvc.filter.FilterAutoConfiguration"
        }
)
@EnableConfigServer
public class ConfigApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigApplication.class, args);
    }
}