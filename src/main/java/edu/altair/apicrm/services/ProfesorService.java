package edu.altair.apicrm.services;

import edu.altair.apicrm.dtos.ProfesorRequestDto;
import edu.altair.apicrm.dtos.ProfesorResponseDto;

import java.util.List;

/**
 * Gestión de profesores.
 */
public interface ProfesorService {

    /**
     * Lista los profesores ordenados por nombre, opcionalmente solo los de un departamento.
     *
     * @param idDepartamento departamento por el que filtrar; si es null se devuelven todos
     * @return los profesores (lista vacía si no hay ninguno)
     * @throws edu.altair.apicrm.exceptions.DepartamentoNotFoundException si se filtra por un departamento inexistente
     */
    List<ProfesorResponseDto> obtenerTodos(Long idDepartamento);

    /**
     * Busca un profesor por su identificador.
     *
     * @param id identificador del profesor
     * @return el profesor
     * @throws edu.altair.apicrm.exceptions.ProfesorNotFoundException si no existe
     */
    ProfesorResponseDto obtenerPorId(Long id);

    /**
     * Da de alta un profesor en un departamento.
     *
     * @param requestDto DNI, nombre y departamento
     * @return el profesor creado
     * @throws edu.altair.apicrm.exceptions.SolicitudInvalidaException    si falta algún dato o no es válido
     * @throws edu.altair.apicrm.exceptions.DepartamentoNotFoundException si el departamento no existe
     * @throws edu.altair.apicrm.exceptions.ConflictoDatosException       si el DNI ya está registrado
     */
    ProfesorResponseDto crear(ProfesorRequestDto requestDto);

    /**
     * Modifica los datos de un profesor, incluido su departamento.
     *
     * @param id         identificador del profesor
     * @param requestDto nuevos datos
     * @return el profesor actualizado
     * @throws edu.altair.apicrm.exceptions.ProfesorNotFoundException     si el profesor no existe
     * @throws edu.altair.apicrm.exceptions.SolicitudInvalidaException    si los datos no son válidos
     * @throws edu.altair.apicrm.exceptions.DepartamentoNotFoundException si el departamento no existe
     * @throws edu.altair.apicrm.exceptions.ConflictoDatosException       si el DNI ya lo usa otro profesor
     */
    ProfesorResponseDto actualizar(Long id, ProfesorRequestDto requestDto);

    /**
     * Elimina un profesor. Solo es posible si no imparte ninguna asignatura, porque toda asignatura
     * debe tener profesor.
     *
     * @param id identificador del profesor
     * @throws edu.altair.apicrm.exceptions.ProfesorNotFoundException si no existe
     * @throws edu.altair.apicrm.exceptions.ConflictoDatosException   si todavía imparte asignaturas
     */
    void eliminar(Long id);

}
