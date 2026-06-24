package com.gestiontaches;

import com.gestiontaches.config.AsyncSyncConfiguration;
import com.gestiontaches.config.EmbeddedSQL;
import com.gestiontaches.config.JacksonConfiguration;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Base composite annotation for integration tests.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(
    classes = {
        GestionTachesApp.class,
        JacksonConfiguration.class,
        AsyncSyncConfiguration.class,
        com.gestiontaches.config.JacksonHibernateConfiguration.class,
    }
)
@EmbeddedSQL
public @interface IntegrationTest {}
