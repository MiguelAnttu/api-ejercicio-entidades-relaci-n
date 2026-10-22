package edu.altair.apicrm.entities;

/**
 * Catálogo de motivos entre los que elige el alumno en el formulario de justificación.
 */
public enum MotivoJustificacion {
    ENFERMEDAD,
    CITA_MEDICA,
    DEBER_INEXCUSABLE,
    FALLECIMIENTO_FAMILIAR,
    TRANSPORTE,
    /** Exige rellenar la descripción. */
    OTRO
}
