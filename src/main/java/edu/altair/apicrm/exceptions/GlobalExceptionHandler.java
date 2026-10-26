package edu.altair.apicrm.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.net.URI;
import java.time.Instant;

/**
 * Traduce las excepciones de la aplicación a respuestas HTTP con formato {@link ProblemDetail} (RFC 9457).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Error 404 - Departamento, profesor, asignatura, estudiante o matrícula inexistente */
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ProblemDetail manejarRecursoNoEncontrado(RecursoNoEncontradoException ex) {
        return crearProblema(HttpStatus.NOT_FOUND, "Recurso No Encontrado", "not-found", ex.getMessage());
    }

    /** Error 400 - Datos de la petición no válidos (reglas de validación del servicio) */
    @ExceptionHandler(SolicitudInvalidaException.class)
    public ProblemDetail manejarSolicitudInvalida(SolicitudInvalidaException ex) {
        return crearProblema(HttpStatus.BAD_REQUEST, "Solicitud No Válida", "bad-request", ex.getMessage());
    }

    /** Error 409 - Dato duplicado (código, DNI, matrícula) o registro con dependencias que impiden borrarlo */
    @ExceptionHandler(ConflictoDatosException.class)
    public ProblemDetail manejarConflicto(ConflictoDatosException ex) {
        return crearProblema(HttpStatus.CONFLICT, "Conflicto de Datos", "conflict", ex.getMessage());
    }

    /** Error 400 - JSON mal formado, fecha inválida o valor de enumerado inexistente en el cuerpo */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail manejarCuerpoIlegible(HttpMessageNotReadableException ex) {
        return crearProblema(HttpStatus.BAD_REQUEST, "Solicitud No Válida", "bad-request",
                "El cuerpo de la petición no es válido: JSON mal formado o valor no permitido en algún campo.");
    }

    /** Error 400 - Parámetro de la URL con un valor del tipo equivocado (p. ej. estado=XYZ) */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail manejarTipoIncorrecto(MethodArgumentTypeMismatchException ex) {
        return crearProblema(HttpStatus.BAD_REQUEST, "Solicitud No Válida", "bad-request",
                "El valor '" + ex.getValue() + "' no es válido para el parámetro '" + ex.getName() + "'.");
    }

    /** Error 400 - Falta un parámetro obligatorio de la URL */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ProblemDetail manejarParametroFaltante(MissingServletRequestParameterException ex) {
        return crearProblema(HttpStatus.BAD_REQUEST, "Solicitud No Válida", "bad-request",
                "Falta el parámetro obligatorio '" + ex.getParameterName() + "'.");
    }

    /** Construye el ProblemDetail común a todas las respuestas de error. */
    private ProblemDetail crearProblema(HttpStatus estado, String titulo, String tipoError, String detalle) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(estado, detalle);
        problem.setTitle(titulo);
        problem.setType(URI.create("https://api.crm.altair.edu/errors/" + tipoError));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

}
