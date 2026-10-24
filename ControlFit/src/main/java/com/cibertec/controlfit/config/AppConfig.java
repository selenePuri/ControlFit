package com.cibertec.controlfit.config;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
//Configuración
@Configuration
public class AppConfig {

    public static final ZoneId ZONA_LIMA = ZoneId.of("America/Lima");

    @Bean
    public Clock clock() {
        return Clock.system(ZONA_LIMA);
    }
}
