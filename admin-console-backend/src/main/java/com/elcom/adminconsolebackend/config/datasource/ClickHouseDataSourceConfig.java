package com.elcom.adminconsolebackend.config.datasource;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        entityManagerFactoryRef = "clickhouseSourceEntityManagerFactory",
        transactionManagerRef = "clickhouseSourceTransactionManager",
        basePackages = "com.elcom.adminconsolebackend.repository.clickhouse"
)
@EntityScan(basePackages = "com.elcom.adminconsolebackend.entity.clickhouse")
public class ClickHouseDataSourceConfig {

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.clickhouse")
    public DataSourceProperties clickhouseSourceProperties() {
        return new DataSourceProperties();
    }


    @Bean("clickhouseSource")
    public DataSource clickhouseSource() {
        return clickhouseSourceProperties()
                .initializeDataSourceBuilder()
                .build();
    }

    @Bean("clickhouseSourceEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean clickhouseSourceEntityManagerFactory(
            @Qualifier("clickhouseSource") DataSource dataSource,
            EntityManagerFactoryBuilder builder
    ) {
        Map<String, Object> jpaProperties = new HashMap<>();
        jpaProperties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        jpaProperties.put("hibernate.physical_naming_strategy", "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy");
        jpaProperties.put("javax.persistence.query.timeout", 60000);

        LocalContainerEntityManagerFactoryBean entityManagerFactoryBean =  builder
                .dataSource(dataSource)
                .packages("com.elcom.adminconsolebackend")
                .persistenceUnit("clickhouseSource")
                .properties(jpaProperties)
                .build();

        JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        entityManagerFactoryBean.setJpaVendorAdapter(vendorAdapter);
        return entityManagerFactoryBean;
    }


    @Bean("clickhouseSourceTransactionManager")
    public PlatformTransactionManager clickhouseSourceTransactionManager(
            @Qualifier("clickhouseSourceEntityManagerFactory") EntityManagerFactory factory
    ) {
        return new JpaTransactionManager(factory);
    }

}
