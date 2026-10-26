package edu.altair.apicrm.dtos;

/**
 * Profesor devuelto por la API.
 *
 * @param idProfesor         identificador interno
 * @param dni                DNI
 * @param nombre             nombre
 * @param idDepartamento     identificador de su departamento
 * @param nombreDepartamento nombre de su departamento
 * @param totalAsignaturas   número de asignaturas que imparte (puede ser 0)
 */
public record ProfesorResponseDto(
    Long idProfesor,
    String dni,
    String nombre,
    Long idDepartamento,
    String nombreDepartamento,
    long totalAsignaturas
) {}
