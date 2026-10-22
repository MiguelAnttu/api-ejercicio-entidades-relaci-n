package edu.altair.apicrm.dtos;

import edu.altair.apicrm.entities.EstadoFalta;

import java.time.LocalDate;

public record FaltaResponseDto(
    Long idFalta,
    Long idAlumno,
    LocalDate fecha,
    Integer numeroSesion,
    String modulo,
    EstadoFalta estado
) {}
