package edu.altair.apicrm.dtos;

import java.time.LocalDate;

/**
 * Datos para matricular a un estudiante en una asignatura.
 *
 * @param idEstudiante       estudiante que se matricula (obligatorio)
 * @param idAsignatura       asignatura en la que se matricula (obligatorio)
 * @param fechaMatriculacion fecha de la matrícula, formato yyyy-MM-dd (opcional: si no se envía, hoy;
 *                           no puede ser futura)
 */
public record MatriculaRequestDto(
    Long idEstudiante,
    Long idAsignatura,
    LocalDate fechaMatriculacion
) {}
