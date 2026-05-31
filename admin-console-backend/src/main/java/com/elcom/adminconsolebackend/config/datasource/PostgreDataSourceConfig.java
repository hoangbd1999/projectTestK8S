package com.elcom.adminconsolebackend.config.datasource;


import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
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
        entityManagerFactoryRef = "managementSourceEntityManagerFactory",
        transactionManagerRef = "managementSourceTransactionManager",
        basePackages = "com.elcom.adminconsolebackend.repository.management"
)
@EntityScan(basePackages = "com.elcom.adminconsolebackend.entity.management")
public class PostgreDataSourceConfig {

    @Bean
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource.management")
    public DataSourceProperties managementSourceProperties() {
        return new DataSourceProperties();
    }

    @Primary
    @Bean("managementSource")
    public DataSource managementSource() {
        return managementSourceProperties()
                .initializeDataSourceBuilder()
                .build();
    }

    @Primary
    @Bean("managementSourceEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean managementSourceEntityManagerFactory(
            @Qualifier("managementSource") DataSource dataSource,
            EntityManagerFactoryBuilder builder
    ) {
        Map<String, String> jpaProperties = new HashMap<>();
        jpaProperties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
      //  jpaProperties.put("hibernate.physical_naming_strategy", SpringPhysicalNamingStrategy.class.getName());
        jpaProperties.put("hibernate.physical_naming_strategy", "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy");
        jpaProperties.put("hibernate.implicit_naming_strategy", SpringImplicitNamingStrategy.class.getName());

        LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = builder
                .dataSource(dataSource)
                .packages("com.elcom.adminconsolebackend")
                .persistenceUnit("managementSource")
                .properties(jpaProperties)
                .build();

        JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        entityManagerFactoryBean.setJpaVendorAdapter(vendorAdapter);
        return entityManagerFactoryBean;
    }

    @Primary
    @Bean("managementSourceTransactionManager")
    public PlatformTransactionManager managementSourceTransactionManager(
            @Qualifier("managementSourceEntityManagerFactory") EntityManagerFactory factory
    ) {
        return new JpaTransactionManager(factory);
    }
}
