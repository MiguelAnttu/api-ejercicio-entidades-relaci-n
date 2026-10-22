package edu.altair.apicrm.dtos;

import edu.altair.apicrm.entities.MotivoJustificacion;

/**
 * Datos editables de una justificación mientras sigue PENDIENTE.
 */
public record JustificacionUpdateDto(
    MotivoJustificacion motivo,
    String descripcion,
    String documentoAdjunto
) {}
