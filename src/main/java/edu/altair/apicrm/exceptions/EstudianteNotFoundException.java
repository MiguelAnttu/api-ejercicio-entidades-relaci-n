package edu.altair.apicrm.exceptions;

/**
 * No existe el recurso estudiante pedido. Se traduce a HTTP 404.
 */
public class EstudianteNotFoundException extends RecursoNoEncontradoException {

    public EstudianteNotFoundException(String mensaje) {
        super(mensaje);
    }

}
