package edu.altair.apicrm.services;

import edu.altair.apicrm.dtos.AsignaturaRequestDto;
import edu.altair.apicrm.dtos.AsignaturaResponseDto;

import java.util.List;

/**
 * Gestión de asignaturas. Una asignatura está activa solo si tiene al menos un estudiante matriculado;
 * ese estado lo mantiene {@link MatriculaService}, no se puede modificar desde aquí.
 */
public interface AsignaturaService {

    /**
     * Lista las asignaturas ordenadas por código, con filtros opcionales y combinables.
     *
     * @param idProfesor profesor que las imparte; si es null no se filtra por profesor
     * @param activa     true para solo las activas, false para solo las inactivas; si es null no se filtra
     * @return las asignaturas (lista vacía si no hay ninguna)
     * @throws edu.altair.apicrm.exceptions.ProfesorNotFoundException si se filtra por un profesor inexistente
     */
    List<AsignaturaResponseDto> obtenerTodas(Long idProfesor, Boolean activa);

    /**
     * Busca una asignatura por su identificador.
     *
     * @param id identificador de la asignatura
     * @return la asignatura
     * @throws edu.altair.apicrm.exceptions.AsignaturaNotFoundException si no existe
     */
    AsignaturaResponseDto obtenerPorId(Long id);

    /**
     * Crea una asignatura asignada a un profesor. Nace inactiva, porque aún no tiene matriculados.
     *
     * @param requestDto código, nombre y profesor
     * @return la asignatura creada
     * @throws edu.altair.apicrm.exceptions.SolicitudInvalidaException  si falta algún dato o no es válido
     * @throws edu.altair.apicrm.exceptions.ProfesorNotFoundException   si el profesor no existe
     * @throws edu.altair.apicrm.exceptions.ConflictoDatosException     si el código ya existe
     */
    AsignaturaResponseDto crear(AsignaturaRequestDto requestDto);

    /**
     * Modifica una asignatura, incluido el profesor que la imparte.
     *
     * @param id         identificador de la asignatura
     * @param requestDto nuevos datos
     * @return la asignatura actualizada
     * @throws edu.altair.apicrm.exceptions.AsignaturaNotFoundException si la asignatura no existe
     * @throws edu.altair.apicrm.exceptions.SolicitudInvalidaException  si los datos no son válidos
     * @throws edu.altair.apicrm.exceptions.ProfesorNotFoundException   si el profesor no existe
     * @throws edu.altair.apicrm.exceptions.ConflictoDatosException     si el código ya lo usa otra asignatura
     */
    AsignaturaResponseDto actualizar(Long id, AsignaturaRequestDto requestDto);

    /**
     * Elimina una asignatura. Solo es posible si no tiene matrículas.
     *
     * @param id identificador de la asignatura
     * @throws edu.altair.apicrm.exceptions.AsignaturaNotFoundException si no existe
     * @throws edu.altair.apicrm.exceptions.ConflictoDatosException     si todavía tiene matrículas
     */
    void eliminar(Long id);

}
