package edu.altair.apicrm.exceptions;

/**
 * No existe el recurso profesor pedido. Se traduce a HTTP 404.
 */
public class ProfesorNotFoundException extends RecursoNoEncontradoException {

    public ProfesorNotFoundException(String mensaje) {
        super(mensaje);
    }

}
