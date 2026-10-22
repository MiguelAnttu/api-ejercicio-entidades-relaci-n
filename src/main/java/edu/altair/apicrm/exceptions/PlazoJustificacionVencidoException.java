package edu.altair.apicrm.exceptions;

/**
 * Ha pasado el plazo permitido para justificar la falta. Se traduce a HTTP 422.
 */
public class PlazoJustificacionVencidoException extends RuntimeException {

    public PlazoJustificacionVencidoException(String mensaje) {
        super(mensaje);
    }

}
