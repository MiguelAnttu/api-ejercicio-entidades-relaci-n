package edu.altair.apicrm.dtos;

/**
 * Datos para crear o modificar un estudiante.
 *
 * @param dni    DNI (obligatorio, único, 8 dígitos y una letra)
 * @param nombre nombre (obligatorio, máximo 100 caracteres)
 */
public record EstudianteRequestDto(
    String dni,
    String nombre
) {}
