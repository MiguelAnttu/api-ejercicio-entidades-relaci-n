package edu.altair.apicrm.dtos;

/**
 * Asignatura devuelta por la API.
 *
 * @param idAsignatura      identificador interno
 * @param codigo            código de la asignatura
 * @param nombre            nombre de la asignatura
 * @param idProfesor        identificador del profesor que la imparte
 * @param nombreProfesor    nombre del profesor que la imparte
 * @param activa            {@code true} si tiene al menos un estudiante matriculado
 * @param totalMatriculados número de estudiantes matriculados
 * @param notaMedia         nota media de las matrículas ya calificadas (null si ninguna tiene nota)
 */
public record AsignaturaResponseDto(
    Long idAsignatura,
    String codigo,
    String nombre,
    Long idProfesor,
    String nombreProfesor,
    boolean activa,
    long totalMatriculados,
    Double notaMedia
) {}
