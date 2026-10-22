package edu.altair.apicrm.services;

import edu.altair.apicrm.dtos.FaltaResponseDto;
import edu.altair.apicrm.dtos.ResumenFaltasResponseDto;
import edu.altair.apicrm.entities.EstadoFalta;

import java.util.List;

public interface FaltaService {

    // Faltas de un alumno; si estado es null se devuelven todas
    List<FaltaResponseDto> obtenerPorAlumno(Long idAlumno, EstadoFalta estado);

    FaltaResponseDto obtenerPorId(Long id);

    ResumenFaltasResponseDto obtenerResumen(Long idAlumno);

}
