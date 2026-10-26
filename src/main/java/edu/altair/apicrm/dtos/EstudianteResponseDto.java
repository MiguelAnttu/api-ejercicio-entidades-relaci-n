package edu.altair.apicrm.dtos;

/**
 * Estudiante devuelto por la API.
 *
 * @param idEstudiante    identificador interno
 * @param dni             DNI
 * @param nombre          nombre
 * @param totalMatriculas número de asignaturas en las que está matriculado (puede ser 0)
 */
public record EstudianteResponseDto(
    Long idEstudiante,
    String dni,
    String nombre,
    long totalMatriculas
) {}
