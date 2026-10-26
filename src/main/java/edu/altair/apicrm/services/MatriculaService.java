package edu.altair.apicrm.services;

import edu.altair.apicrm.dtos.MatriculaRequestDto;
import edu.altair.apicrm.dtos.MatriculaResponseDto;
import edu.altair.apicrm.dtos.NotaRequestDto;

import java.util.List;

/**
 * Gestión de las matrículas de los estudiantes en las asignaturas. Es el único servicio que cambia el
 * estado "activa" de una asignatura: la primera matrícula la activa y anular la última la desactiva.
 */
public interface MatriculaService {

    /**
     * Lista matrículas con filtros opcionales y combinables.
     *
     * @param idEstudiante estudiante por el que filtrar; si es null no se filtra por estudiante
     * @param idAsignatura asignatura por la que filtrar; si es null no se filtra por asignatura
     * @return las matrículas (lista vacía si no hay ninguna)
     * @throws edu.altair.apicrm.exceptions.EstudianteNotFoundException si se filtra por un estudiante inexistente
     * @throws edu.altair.apicrm.exceptions.AsignaturaNotFoundException si se filtra por una asignatura inexistente
     */
    List<MatriculaResponseDto> obtenerTodas(Long idEstudiante, Long idAsignatura);

    /**
     * Busca una matrícula por su identificador.
     *
     * @param id identificador de la matrícula
     * @return la matrícula
     * @throws edu.altair.apicrm.exceptions.MatriculaNotFoundException si no existe
     */
    MatriculaResponseDto obtenerPorId(Long id);

    /**
     * Matricula a un estudiante en una asignatura y, si era la primera matrícula, activa la asignatura.
     * La matrícula nace sin nota.
     *
     * @param requestDto estudiante, asignatura y fecha opcional (por defecto hoy)
     * @return la matrícula creada
     * @throws edu.altair.apicrm.exceptions.SolicitudInvalidaException  si falta algún dato o la fecha es futura
     * @throws edu.altair.apicrm.exceptions.EstudianteNotFoundException si el estudiante no existe
     * @throws edu.altair.apicrm.exceptions.AsignaturaNotFoundException si la asignatura no existe
     * @throws edu.altair.apicrm.exceptions.ConflictoDatosException     si ya estaba matriculado en esa asignatura
     */
    MatriculaResponseDto matricular(MatriculaRequestDto requestDto);

    /**
     * Registra, cambia o borra la nota final de una matrícula.
     *
     * @param id         identificador de la matrícula
     * @param requestDto nota entre 0 y 10 con hasta dos decimales, o null para borrarla
     * @return la matrícula con la nota actualizada
     * @throws edu.altair.apicrm.exceptions.MatriculaNotFoundException si no existe
     * @throws edu.altair.apicrm.exceptions.SolicitudInvalidaException si la nota está fuera de rango o tiene más de dos decimales
     */
    MatriculaResponseDto calificar(Long id, NotaRequestDto requestDto);

    /**
     * Anula una matrícula. Si era la última de la asignatura, esta se desactiva.
     *
     * @param id identificador de la matrícula
     * @throws edu.altair.apicrm.exceptions.MatriculaNotFoundException si no existe
     */
    void anular(Long id);

}
