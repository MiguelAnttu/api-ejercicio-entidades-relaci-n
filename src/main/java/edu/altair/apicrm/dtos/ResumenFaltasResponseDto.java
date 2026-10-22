package edu.altair.apicrm.dtos;

/**
 * Contadores que muestra la cabecera de la pantalla "Mis faltas" del alumno.
 */
public record ResumenFaltasResponseDto(
    Long idAlumno,
    long totalFaltas,
    long sinJustificar,
    long enRevision,
    long justificadas
) {}
