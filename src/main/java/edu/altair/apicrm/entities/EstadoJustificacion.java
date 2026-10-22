package edu.altair.apicrm.entities;

/**
 * Ciclo de vida de una justificación enviada por el alumno.
 */
public enum EstadoJustificacion {
    /** Enviada y a la espera de revisión. Solo en este estado se puede editar o retirar. */
    PENDIENTE,
    /** Aceptada por el tutor: la falta pasa a JUSTIFICADA. */
    APROBADA,
    /** Rechazada por el tutor: la falta vuelve a SIN_JUSTIFICAR. */
    RECHAZADA
}
