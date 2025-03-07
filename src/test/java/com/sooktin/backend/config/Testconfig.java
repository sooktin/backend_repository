package com.sooktin.backend.config;

import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class Testconfig {

    @Bean
    public JpaProperties jpaProperties() {
        JpaProperties jpaProperties = new JpaProperties();
        jpaProperties.getProperties().put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        return jpaProperties;
    }
}
