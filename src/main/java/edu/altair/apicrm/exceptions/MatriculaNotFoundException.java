package edu.altair.apicrm.exceptions;

/**
 * No existe el recurso matricula pedido. Se traduce a HTTP 404.
 */
public class MatriculaNotFoundException extends RecursoNoEncontradoException {

    public MatriculaNotFoundException(String mensaje) {
        super(mensaje);
    }

}
