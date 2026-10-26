package edu.altair.apicrm.services;

import edu.altair.apicrm.dtos.EstudianteRequestDto;
import edu.altair.apicrm.dtos.EstudianteResponseDto;

import java.util.List;

/**
 * Gestión de estudiantes. Las matrículas se gestionan en {@link MatriculaService}.
 */
public interface EstudianteService {

    /**
     * Lista todos los estudiantes ordenados por nombre.
     *
     * @return los estudiantes (lista vacía si no hay ninguno)
     */
    List<EstudianteResponseDto> obtenerTodos();

    /**
     * Busca un estudiante por su identificador.
     *
     * @param id identificador del estudiante
     * @return el estudiante
     * @throws edu.altair.apicrm.exceptions.EstudianteNotFoundException si no existe
     */
    EstudianteResponseDto obtenerPorId(Long id);

    /**
     * Da de alta un estudiante. Puede no estar matriculado en ninguna asignatura.
     *
     * @param requestDto DNI y nombre
     * @return el estudiante creado
     * @throws edu.altair.apicrm.exceptions.SolicitudInvalidaException si falta algún dato o no es válido
     * @throws edu.altair.apicrm.exceptions.ConflictoDatosException    si el DNI ya está registrado
     */
    EstudianteResponseDto crear(EstudianteRequestDto requestDto);

    /**
     * Modifica los datos de un estudiante.
     *
     * @param id         identificador del estudiante
     * @param requestDto nuevos datos
     * @return el estudiante actualizado
     * @throws edu.altair.apicrm.exceptions.EstudianteNotFoundException si no existe
     * @throws edu.altair.apicrm.exceptions.SolicitudInvalidaException  si los datos no son válidos
     * @throws edu.altair.apicrm.exceptions.ConflictoDatosException     si el DNI ya lo usa otro estudiante
     */
    EstudianteResponseDto actualizar(Long id, EstudianteRequestDto requestDto);

    /**
     * Elimina un estudiante. Solo es posible si no tiene matrículas.
     *
     * @param id identificador del estudiante
     * @throws edu.altair.apicrm.exceptions.EstudianteNotFoundException si no existe
     * @throws edu.altair.apicrm.exceptions.ConflictoDatosException     si todavía tiene matrículas
     */
    void eliminar(Long id);

}
