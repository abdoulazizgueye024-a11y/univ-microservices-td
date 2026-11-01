package com.ecommerce.launchers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(
        scanBasePackages = {
                "com.ecommerce.univ.catalogue",
                "com.ecommerce.catalogue.config"
        },excludeName = {
        "org.springframework.cloud.gateway.server.mvc.filter.FilterAutoConfiguration"
}
)
@EntityScan(basePackages = "com.ecommerce.univ.catalogue")
@EnableJpaRepositories(basePackages = "com.ecommerce.univ.catalogue")
public class CatalogueApplication {

    public static void main(String[] args) {
        SpringApplication.run(CatalogueApplication.class, args);
    }
}