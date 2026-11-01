package com.ecommerce.launchers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication(
        scanBasePackages = "com.ecommerce.eureka",
        excludeName = {
                "org.springframework.cloud.gateway.server.mvc.filter.FilterAutoConfiguration"
        }
)
@EnableEurekaServer
public class EurekaApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaApplication.class, args);
    }
}