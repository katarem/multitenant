package com.katarem.multitenant;

import com.katarem.multitenant.routing.TenantRouting;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AutoConfiguration
@EnableConfigurationProperties(MultipleDataSourceProperties.class)
@ConditionalOnProperty(prefix = "app.datasources", name = "configs")
public class MultiTenantAutoConfiguration implements DisposableBean {

    private static final Logger logger = LoggerFactory.getLogger(MultiTenantAutoConfiguration.class);
    private final List<HikariDataSource> pools = new ArrayList<>();

    @Bean
    @Primary
    @ConditionalOnMissingBean(DataSource.class)
    DataSource dataSource(MultipleDataSourceProperties props) {

        if (props.getConfigs() == null || props.getConfigs().isEmpty()) {
            throw new IllegalStateException("app.datasources.configs is required and cannot be empty");
        }

        Map<Object, Object> targets = new HashMap<>();

        props.getConfigs().forEach((name, config) -> {

            HikariDataSource dataSource = new HikariDataSource();

            dataSource.setJdbcUrl(config.getUrl());
            dataSource.setUsername(config.getUsername());
            dataSource.setPassword(config.getPassword());
            if (config.getDriverClassName() != null && !config.getDriverClassName().isBlank()) {
                dataSource.setDriverClassName(config.getDriverClassName());
            }
            dataSource.setPoolName("ds-" + name);
            targets.put(name, dataSource);
            pools.add(dataSource);
        });

        Object defaultDs = targets.get(props.getDefaultDs());
        if (defaultDs == null) {
            throw new IllegalStateException("Default datasource '" + props.getDefaultDs() + "' not found");
        }

        TenantRouting routing = new TenantRouting();
        routing.setTargetDataSources(targets);
        routing.setDefaultTargetDataSource(targets.get(props.getDefaultDs()));
        routing.afterPropertiesSet();

        logger.info("[multitenant-starter] Enabled. Routing DataSource initialized. default='{}', datasources={}",
                props.getDefaultDs(), targets.keySet());

        if (logger.isDebugEnabled()) {
            props.getConfigs().forEach((name, cfg) ->
                    logger.debug("[multitenant-starter] datasource='{}' url='{}' user='{}'",
                            name, safeUrl(cfg.getUrl()), cfg.getUsername())
            );
        }

        return routing;
    }

    @Override
    public void destroy() {
        pools.forEach(HikariDataSource::close);
        logger.info("[multitenant-starter] Shutdown. Closed {} datasource pool(s).", pools.size());
    }

    private static String safeUrl(String url) {
        if (url == null) return null;
        int idx = url.indexOf('?');
        return idx >= 0 ? url.substring(0, idx) : url;
    }

}
