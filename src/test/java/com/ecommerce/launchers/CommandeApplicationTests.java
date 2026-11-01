package com.ecommerce.launchers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.lang.reflect.Method;
import static org.junit.jupiter.api.Assertions.*;

/** Test structurel sans lancer les services externes (Eureka, PostgreSQL, Config Server). */
class CommandeApplicationTests {
    @Test
    void launcherEstUneApplicationSpringBoot() {
        assertNotNull(CommandeApplication.class.getAnnotation(SpringBootApplication.class));
    }

    @Test
    void possedeUneMethodeMain() throws Exception {
        Method main = CommandeApplication.class.getMethod("main", String[].class);
        assertTrue(java.lang.reflect.Modifier.isStatic(main.getModifiers()));
    }
}
