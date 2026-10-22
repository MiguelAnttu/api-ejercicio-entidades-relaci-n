package edu.altair.apicrm.exceptions;

/**
 * La operación no es posible por el estado actual de la falta o de la justificación. Se traduce a HTTP 409.
 */
public class ConflictoJustificacionException extends RuntimeException {

    public ConflictoJustificacionException(String mensaje) {
        super(mensaje);
    }

}
