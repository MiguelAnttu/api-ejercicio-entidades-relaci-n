package edu.altair.apicrm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada de la API REST del CRM de Altair (departamentos, profesores, asignaturas,
 * estudiantes y matrículas).
 */
@SpringBootApplication
public class ApicrmApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApicrmApplication.class, args);
    }

}
