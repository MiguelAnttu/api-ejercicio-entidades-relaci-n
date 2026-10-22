package edu.altair.apicrm.services;

import edu.altair.apicrm.dtos.JustificacionRequestDto;
import edu.altair.apicrm.dtos.JustificacionResponseDto;
import edu.altair.apicrm.dtos.JustificacionUpdateDto;
import edu.altair.apicrm.dtos.ResolucionRequestDto;
import edu.altair.apicrm.entities.EstadoJustificacion;

import java.util.List;

public interface JustificacionService {

    // Historial de justificaciones de un alumno; si estado es null se devuelven todas
    List<JustificacionResponseDto> obtenerPorAlumno(Long idAlumno, EstadoJustificacion estado);

    JustificacionResponseDto obtenerPorId(Long id);

    // Crea una justificación por cada falta indicada (operación atómica)
    List<JustificacionResponseDto> crear(JustificacionRequestDto requestDto);

    // Edita una justificación mientras está PENDIENTE
    JustificacionResponseDto actualizar(Long id, JustificacionUpdateDto requestDto);

    // El tutor aprueba o rechaza una justificación PENDIENTE
    JustificacionResponseDto resolver(Long id, ResolucionRequestDto requestDto);

    // El alumno retira una justificación PENDIENTE
    void eliminar(Long id);

}
