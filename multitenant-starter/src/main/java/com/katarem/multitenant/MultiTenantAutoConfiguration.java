package com.katarem.multitenant;

import com.katarem.multitenant.routing.TenantRouting;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@AutoConfiguration
@EnableConfigurationProperties(MultipleDataSourceProperties.class)
@ConditionalOnClass({DataSource.class, HikariDataSource.class})
public class MultiTenantAutoConfiguration implements DisposableBean {

    private static final Logger logger = LoggerFactory.getLogger(MultiTenantAutoConfiguration.class);
    private final List<HikariDataSource> pools = new ArrayList<>();

    private static final AtomicBoolean logged = new AtomicBoolean(false);

    @Bean
    @Primary
    @ConditionalOnMissingBean(DataSource.class)
    DataSource dataSource(MultipleDataSourceProperties props) {

        if (props.getDatasources() == null || props.getDatasources().isEmpty()) {
            throw new IllegalStateException("katarem.mutltenant.datasources is required and cannot be empty");
        }

        Map<Object, Object> targets = new HashMap<>();

        props.getDatasources().forEach((name, config) -> {

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

        String defaultKey = props.getDefaultDatasource();
        if (defaultKey == null || defaultKey.isBlank()) {
            throw new IllegalStateException("katarem.multitenant.default is required");
        }

        if (!targets.containsKey(defaultKey)) {
            throw new IllegalStateException(
                    "Default datasource '" + defaultKey + "' not found. Available: " + targets.keySet()
            );
        }

        Object defaultDs = targets.get(props.getDefaultDatasource());
        if (defaultDs == null) {
            throw new IllegalStateException("Default datasource '" + defaultKey + "' not found");
        }

        TenantRouting routing = new TenantRouting();
        routing.setTargetDataSources(targets);
        routing.setDefaultTargetDataSource(targets.get(props.getDefaultDatasource()));
        routing.afterPropertiesSet();

        if(logged.compareAndSet(false, true)) {
            logger.info("""
                [katarem-multitenant] Multi-tenant datasource routing enabled
                Default datasource : {}
                Configured tenants : {}
                Datasource pools   : {}
                """, defaultKey, targets.keySet(), pools.size());
        }

        if (logger.isDebugEnabled() && logged.compareAndSet(false, true)) {
            props.getDatasources().forEach((name, config) ->
                    logger.debug("[katarem-multitenant] datasource='{}' url='{}' user='{}'",
                            name, safeUrl(config.getUrl()), config.getUsername())
            );
        }

        return routing;
    }

    @Override
    public void destroy() {
        pools.forEach(HikariDataSource::close);
        if(logged.compareAndSet(false, true)){
            logger.info("[katarem-multitenant] Shutdown. Closed {} datasource pool(s).", pools.size());
        }
    }

    private static String safeUrl(String url) {
        if (url == null) return null;
        int idx = url.indexOf('?');
        return idx >= 0 ? url.substring(0, idx) : url;
    }

}
