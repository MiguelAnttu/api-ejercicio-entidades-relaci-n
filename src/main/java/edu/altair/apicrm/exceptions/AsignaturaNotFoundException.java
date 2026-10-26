package edu.altair.apicrm.exceptions;

/**
 * No existe el recurso asignatura pedido. Se traduce a HTTP 404.
 */
public class AsignaturaNotFoundException extends RecursoNoEncontradoException {

    public AsignaturaNotFoundException(String mensaje) {
        super(mensaje);
    }

}
