package edu.altair.apicrm.dtos;

import edu.altair.apicrm.entities.MotivoJustificacion;

import java.util.List;

/**
 * Datos del formulario "Justificar faltas": el alumno marca una o varias faltas, elige un motivo,
 * puede describir lo ocurrido y adjuntar la referencia del justificante.
 */
public record JustificacionRequestDto(
    Long idAlumno,
    List<Long> idsFaltas,
    MotivoJustificacion motivo,
    String descripcion,
    String documentoAdjunto
) {}
