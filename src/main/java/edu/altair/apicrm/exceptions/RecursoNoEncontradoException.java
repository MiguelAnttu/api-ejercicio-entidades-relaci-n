package edu.altair.apicrm.exceptions;

/**
 * Base de las excepciones "no existe el recurso pedido". Se traduce a HTTP 404.
 */
public abstract class RecursoNoEncontradoException extends RuntimeException {

    protected RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }

}
