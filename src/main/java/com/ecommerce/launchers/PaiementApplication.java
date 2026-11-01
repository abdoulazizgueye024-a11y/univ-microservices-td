package com.ecommerce.launchers;

import com.ecommerce.univ.paiement.domain.Paiement;
import com.ecommerce.univ.paiement.repository.PaiementRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(
        scanBasePackages = {
                "com.ecommerce.univ.paiement"
        },excludeName = {
        "org.springframework.cloud.gateway.server.mvc.filter.FilterAutoConfiguration"
})
@EntityScan(basePackageClasses = Paiement.class)
@EnableJpaRepositories(basePackageClasses = PaiementRepository.class)
@EnableFeignClients(basePackages = "com.ecommerce.univ.paiement.client")
public class PaiementApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaiementApplication.class, args);
    }
}