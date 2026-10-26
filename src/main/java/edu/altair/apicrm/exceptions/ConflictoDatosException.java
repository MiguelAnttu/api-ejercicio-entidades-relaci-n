package edu.altair.apicrm.exceptions;

/**
 * La operación choca con el estado actual de los datos: código o DNI ya registrado, matrícula duplicada
 * o borrado de un registro del que todavía dependen otros. Se traduce a HTTP 409.
 */
public class ConflictoDatosException extends RuntimeException {

    public ConflictoDatosException(String mensaje) {
        super(mensaje);
    }

}
