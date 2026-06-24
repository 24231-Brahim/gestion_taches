package com.gestiontaches.config;

import com.github.benmanes.caffeine.jcache.configuration.CaffeineConfiguration;
import java.util.OptionalLong;
import java.util.concurrent.TimeUnit;
import org.hibernate.cache.jcache.ConfigSettings;
import org.springframework.boot.cache.autoconfigure.JCacheManagerCustomizer;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tech.jhipster.config.JHipsterProperties;

@Configuration
@EnableCaching
public class CacheConfiguration {

    private final javax.cache.configuration.Configuration<Object, Object> jcacheConfiguration;

    public CacheConfiguration(JHipsterProperties jHipsterProperties) {
        JHipsterProperties.Cache.Caffeine caffeine = jHipsterProperties.getCache().getCaffeine();

        CaffeineConfiguration<Object, Object> caffeineConfiguration = new CaffeineConfiguration<>();
        caffeineConfiguration.setMaximumSize(OptionalLong.of(caffeine.getMaxEntries()));
        caffeineConfiguration.setExpireAfterWrite(OptionalLong.of(TimeUnit.SECONDS.toNanos(caffeine.getTimeToLiveSeconds())));
        caffeineConfiguration.setStatisticsEnabled(true);
        jcacheConfiguration = caffeineConfiguration;
    }

    @Bean
    public HibernatePropertiesCustomizer hibernatePropertiesCustomizer(javax.cache.CacheManager cacheManager) {
        return hibernateProperties -> hibernateProperties.put(ConfigSettings.CACHE_MANAGER, cacheManager);
    }

    @Bean
    public JCacheManagerCustomizer cacheManagerCustomizer() {
        return cm -> {
            createCache(cm, com.gestiontaches.repository.UserRepository.USERS_BY_LOGIN_CACHE);
            createCache(cm, com.gestiontaches.repository.UserRepository.USERS_BY_EMAIL_CACHE);
            createCache(cm, com.gestiontaches.domain.User.class.getName());
            createCache(cm, com.gestiontaches.domain.Authority.class.getName());
            createCache(cm, com.gestiontaches.domain.User.class.getName() + ".authorities");
            createCache(cm, com.gestiontaches.domain.Project.class.getName());
            createCache(cm, com.gestiontaches.domain.Project.class.getName() + ".sprintses");
            createCache(cm, com.gestiontaches.domain.Project.class.getName() + ".epicses");
            createCache(cm, com.gestiontaches.domain.Project.class.getName() + ".issueses");
            createCache(cm, com.gestiontaches.domain.Sprint.class.getName());
            createCache(cm, com.gestiontaches.domain.Epic.class.getName());
            createCache(cm, com.gestiontaches.domain.Issue.class.getName());
            createCache(cm, com.gestiontaches.domain.Issue.class.getName() + ".commentses");
            createCache(cm, com.gestiontaches.domain.Issue.class.getName() + ".attachmentses");
            createCache(cm, com.gestiontaches.domain.Issue.class.getName() + ".histories");
            createCache(cm, com.gestiontaches.domain.Comment.class.getName());
            createCache(cm, com.gestiontaches.domain.Attachment.class.getName());
            createCache(cm, com.gestiontaches.domain.ActionHistory.class.getName());
            // jhipster-needle-caffeine-add-entry
        };
    }

    private void createCache(javax.cache.CacheManager cm, String cacheName) {
        javax.cache.Cache<Object, Object> cache = cm.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        } else {
            cm.createCache(cacheName, jcacheConfiguration);
        }
    }
}
