package edu.altair.apicrm.exceptions;

/**
 * Los datos enviados por el cliente no cumplen las reglas de validación. Se traduce a HTTP 400.
 */
public class SolicitudInvalidaException extends RuntimeException {

    public SolicitudInvalidaException(String mensaje) {
        super(mensaje);
    }

}
