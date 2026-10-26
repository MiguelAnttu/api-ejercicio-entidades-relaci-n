package edu.altair.apicrm.services.impl;

import edu.altair.apicrm.exceptions.SolicitudInvalidaException;

import java.util.regex.Pattern;

/**
 * Validaciones y limpieza de texto compartidas por las implementaciones de los servicios.
 */
final class Validaciones {

    private static final Pattern PATRON_DNI = Pattern.compile("\\d{8}[A-Za-z]");

    private Validaciones() {}

    /**
     * Quita espacios sobrantes y convierte las cadenas vacías en {@code null}.
     *
     * @param texto texto original (puede ser null)
     * @return el texto sin espacios en los extremos, o null si estaba vacío
     */
    static String limpiar(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        return texto.trim();
    }

    /**
     * Comprueba que un campo de texto obligatorio viene informado y no supera la longitud máxima.
     *
     * @param campo  nombre del campo, para el mensaje de error
     * @param valor  valor recibido
     * @param maximo longitud máxima permitida
     * @return el valor sin espacios en los extremos
     * @throws SolicitudInvalidaException si falta o es demasiado largo
     */
    static String textoObligatorio(String campo, String valor, int maximo) {
        String limpio = limpiar(valor);
        if (limpio == null) {
            throw new SolicitudInvalidaException("El campo " + campo + " es obligatorio.");
        }
        if (limpio.length() > maximo) {
            throw new SolicitudInvalidaException(
                    "El campo " + campo + " no puede superar los " + maximo + " caracteres.");
        }
        return limpio;
    }

    /**
     * Comprueba que el DNI tiene 8 dígitos y una letra, y lo devuelve normalizado en mayúsculas.
     *
     * @param dni DNI recibido
     * @return el DNI sin espacios y en mayúsculas
     * @throws SolicitudInvalidaException si falta o no tiene el formato correcto
     */
    static String dniValido(String dni) {
        String limpio = limpiar(dni);
        if (limpio == null) {
            throw new SolicitudInvalidaException("El campo dni es obligatorio.");
        }
        if (!PATRON_DNI.matcher(limpio).matches()) {
            throw new SolicitudInvalidaException("El dni debe tener 8 dígitos y una letra (p. ej. 12345678A).");
        }
        return limpio.toUpperCase();
    }

    /**
     * Comprueba que un identificador obligatorio viene informado.
     *
     * @param campo nombre del campo, para el mensaje de error
     * @param id    valor recibido
     * @throws SolicitudInvalidaException si es null
     */
    static void idObligatorio(String campo, Long id) {
        if (id == null) {
            throw new SolicitudInvalidaException("El campo " + campo + " es obligatorio.");
        }
    }
}
