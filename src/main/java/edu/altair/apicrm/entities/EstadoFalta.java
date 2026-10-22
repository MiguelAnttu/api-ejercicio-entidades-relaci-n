package edu.altair.apicrm.entities;

/**
 * Situación de una falta de asistencia desde el punto de vista de su justificación.
 */
public enum EstadoFalta {
    /** No tiene ninguna justificación activa. Es el único estado desde el que se puede justificar. */
    SIN_JUSTIFICAR,
    /** El alumno ha enviado una justificación que está pendiente de que el tutor la resuelva. */
    EN_REVISION,
    /** Justificación aprobada. */
    JUSTIFICADA
}
