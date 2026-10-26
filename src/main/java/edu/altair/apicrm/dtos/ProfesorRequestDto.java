package edu.altair.apicrm.dtos;

/**
 * Datos para crear o modificar un profesor.
 *
 * @param dni            DNI (obligatorio, único, 8 dígitos y una letra)
 * @param nombre         nombre (obligatorio, máximo 100 caracteres)
 * @param idDepartamento departamento al que pertenece (obligatorio: todo profesor tiene uno)
 */
public record ProfesorRequestDto(
    String dni,
    String nombre,
    Long idDepartamento
) {}
