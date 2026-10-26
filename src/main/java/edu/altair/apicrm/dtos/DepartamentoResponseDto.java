package edu.altair.apicrm.dtos;

/**
 * Departamento devuelto por la API.
 *
 * @param idDepartamento  identificador interno
 * @param codigo          código del departamento
 * @param nombre          nombre del departamento
 * @param totalProfesores número de profesores contratados (puede ser 0)
 */
public record DepartamentoResponseDto(
    Long idDepartamento,
    String codigo,
    String nombre,
    long totalProfesores
) {}
