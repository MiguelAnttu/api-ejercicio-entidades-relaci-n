package edu.altair.apicrm.dtos;

/**
 * Datos para crear o modificar una asignatura. El estado "activa" no se envía: lo calcula la API
 * según haya o no estudiantes matriculados.
 *
 * @param codigo     código de la asignatura (obligatorio, único, máximo 10 caracteres)
 * @param nombre     nombre de la asignatura (obligatorio, máximo 100 caracteres)
 * @param idProfesor profesor que la imparte (obligatorio: toda asignatura tiene uno)
 */
public record AsignaturaRequestDto(
    String codigo,
    String nombre,
    Long idProfesor
) {}
