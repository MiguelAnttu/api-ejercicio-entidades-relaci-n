package edu.altair.apicrm.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Matrícula devuelta por la API.
 *
 * @param idMatricula        identificador interno
 * @param idEstudiante       identificador del estudiante
 * @param nombreEstudiante   nombre del estudiante
 * @param idAsignatura       identificador de la asignatura
 * @param nombreAsignatura   nombre de la asignatura
 * @param fechaMatriculacion fecha de la matrícula
 * @param notaFinal          nota final (null mientras no esté calificada)
 */
public record MatriculaResponseDto(
    Long idMatricula,
    Long idEstudiante,
    String nombreEstudiante,
    Long idAsignatura,
    String nombreAsignatura,
    LocalDate fechaMatriculacion,
    BigDecimal notaFinal
) {}
