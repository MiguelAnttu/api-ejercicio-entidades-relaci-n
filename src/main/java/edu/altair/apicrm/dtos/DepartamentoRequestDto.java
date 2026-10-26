package edu.altair.apicrm.dtos;

/**
 * Datos para crear o modificar un departamento.
 *
 * @param codigo código del departamento (obligatorio, único, máximo 10 caracteres)
 * @param nombre nombre del departamento (obligatorio, máximo 100 caracteres)
 */
public record DepartamentoRequestDto(
    String codigo,
    String nombre
) {}
