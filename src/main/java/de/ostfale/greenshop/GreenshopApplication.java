package de.ostfale.greenshop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class GreenshopApplication {

    public static void main(String[] args) {
        SpringApplication.run(GreenshopApplication.class, args);
    }

}
