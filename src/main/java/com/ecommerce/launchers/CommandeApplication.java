package com.ecommerce.launchers;

import com.ecommerce.univ.commande.domain.Commande;
import com.ecommerce.univ.commande.repository.CommandeRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(
        scanBasePackages = {
                "com.ecommerce.univ.commande"
        },excludeName = {
        "org.springframework.cloud.gateway.server.mvc.filter.FilterAutoConfiguration"
})
@EntityScan(basePackageClasses = Commande.class)
@EnableJpaRepositories(
        basePackageClasses = CommandeRepository.class
)
@EnableFeignClients(
        basePackages = "com.ecommerce.univ.commande.client"
)
public class CommandeApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommandeApplication.class, args);
    }
}