package edu.altair.apicrm.dtos;

import edu.altair.apicrm.entities.EstadoJustificacion;

/**
 * Decisión del tutor sobre una justificación. Solo se admiten los estados APROBADA y RECHAZADA.
 */
public record ResolucionRequestDto(
    EstadoJustificacion estado,
    String comentarioRevision
) {}
