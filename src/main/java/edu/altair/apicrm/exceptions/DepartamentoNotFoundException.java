package edu.altair.apicrm.exceptions;

/**
 * No existe el recurso departamento pedido. Se traduce a HTTP 404.
 */
public class DepartamentoNotFoundException extends RecursoNoEncontradoException {

    public DepartamentoNotFoundException(String mensaje) {
        super(mensaje);
    }

}
