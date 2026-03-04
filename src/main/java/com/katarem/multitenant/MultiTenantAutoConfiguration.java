package com.katarem.multitenant;

import com.katarem.multitenant.routing.TenantRouting;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.AutoConfiguration;
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
public class MultiTenantAutoConfiguration {

    @Bean
    @Primary
    DataSource dataSource(MultipleDataSourceProperties props) {
        Map<Object, Object> targets = new HashMap<>();
        List<HikariDataSource> pools = new ArrayList<>();

        props.getConfigs().forEach((name, config) -> {

            HikariDataSource dataSource = new HikariDataSource();

            dataSource.setJdbcUrl(config.getUrl());
            dataSource.setUsername(config.getUsername());
            dataSource.setPassword(config.getPassword());

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

        Runtime.getRuntime().addShutdownHook(new Thread(() -> pools.forEach(HikariDataSource::close)));

        return routing;
    }

}
