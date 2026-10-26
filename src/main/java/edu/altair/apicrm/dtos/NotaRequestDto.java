package edu.altair.apicrm.dtos;

import java.math.BigDecimal;

/**
 * Datos para calificar una matrícula.
 *
 * @param notaFinal nota final entre 0 y 10 con hasta dos decimales; si es null se borra la nota
 */
public record NotaRequestDto(
    BigDecimal notaFinal
) {}
