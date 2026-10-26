package edu.altair.apicrm.services;

import edu.altair.apicrm.dtos.DepartamentoRequestDto;
import edu.altair.apicrm.dtos.DepartamentoResponseDto;

import java.util.List;

/**
 * Gestión de departamentos académicos.
 */
public interface DepartamentoService {

    /**
     * Lista todos los departamentos ordenados por código.
     *
     * @return los departamentos (lista vacía si no hay ninguno)
     */
    List<DepartamentoResponseDto> obtenerTodos();

    /**
     * Busca un departamento por su identificador.
     *
     * @param id identificador del departamento
     * @return el departamento
     * @throws edu.altair.apicrm.exceptions.DepartamentoNotFoundException si no existe
     */
    DepartamentoResponseDto obtenerPorId(Long id);

    /**
     * Crea un departamento.
     *
     * @param requestDto código y nombre
     * @return el departamento creado
     * @throws edu.altair.apicrm.exceptions.SolicitudInvalidaException si falta algún dato o es demasiado largo
     * @throws edu.altair.apicrm.exceptions.ConflictoDatosException    si el código ya existe
     */
    DepartamentoResponseDto crear(DepartamentoRequestDto requestDto);

    /**
     * Modifica el código y el nombre de un departamento.
     *
     * @param id         identificador del departamento
     * @param requestDto nuevos datos
     * @return el departamento actualizado
     * @throws edu.altair.apicrm.exceptions.DepartamentoNotFoundException si no existe
     * @throws edu.altair.apicrm.exceptions.SolicitudInvalidaException    si los datos no son válidos
     * @throws edu.altair.apicrm.exceptions.ConflictoDatosException       si el código ya lo usa otro departamento
     */
    DepartamentoResponseDto actualizar(Long id, DepartamentoRequestDto requestDto);

    /**
     * Elimina un departamento. Solo es posible si no tiene profesores contratados, porque todo profesor
     * debe pertenecer a un departamento.
     *
     * @param id identificador del departamento
     * @throws edu.altair.apicrm.exceptions.DepartamentoNotFoundException si no existe
     * @throws edu.altair.apicrm.exceptions.ConflictoDatosException       si todavía tiene profesores
     */
    void eliminar(Long id);

}
