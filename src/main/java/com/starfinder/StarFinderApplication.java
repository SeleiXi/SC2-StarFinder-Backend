package com.starfinder;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

@SpringBootApplication
@EnableScheduling
@MapperScan("com.starfinder.mapper")
public class StarFinderApplication {
    public static void main(String[] args) {
        SpringApplication.run(StarFinderApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);   // 5 second connect timeout
        factory.setReadTimeout(10000);     // 10 second read timeout
        return new RestTemplate(factory);
    }
}