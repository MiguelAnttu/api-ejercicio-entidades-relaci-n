package edu.altair.apicrm.services.impl;

import edu.altair.apicrm.dtos.FaltaResponseDto;
import edu.altair.apicrm.dtos.ResumenFaltasResponseDto;
import edu.altair.apicrm.entities.EstadoFalta;
import edu.altair.apicrm.entities.Falta;
import edu.altair.apicrm.exceptions.AlumnoNotFoundException;
import edu.altair.apicrm.exceptions.FaltaNotFoundException;
import edu.altair.apicrm.repositories.AlumnoRepository;
import edu.altair.apicrm.repositories.FaltaRepository;
import edu.altair.apicrm.services.FaltaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FaltaServiceImpl implements FaltaService {

    private final FaltaRepository faltaRepository;
    private final AlumnoRepository alumnoRepository;

    // Inyección de dependencias por constructor
    public FaltaServiceImpl(FaltaRepository faltaRepository, AlumnoRepository alumnoRepository) {
        this.faltaRepository = faltaRepository;
        this.alumnoRepository = alumnoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<FaltaResponseDto> obtenerPorAlumno(Long idAlumno, EstadoFalta estado) {
        comprobarAlumno(idAlumno);

        List<Falta> faltas = (estado == null)
                ? faltaRepository.findByAlumno_IdAlumnoOrderByFechaDescNumeroSesionAsc(idAlumno)
                : faltaRepository.findByAlumno_IdAlumnoAndEstadoOrderByFechaDescNumeroSesionAsc(idAlumno, estado);

        return faltas.stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public FaltaResponseDto obtenerPorId(Long id) {
        Falta falta = faltaRepository.findById(id)
                .orElseThrow(() -> new FaltaNotFoundException("Falta no encontrada con el ID: " + id));
        return mapToResponseDto(falta);
    }

    @Override
    @Transactional(readOnly = true)
    public ResumenFaltasResponseDto obtenerResumen(Long idAlumno) {
        comprobarAlumno(idAlumno);

        long sinJustificar = faltaRepository.countByAlumno_IdAlumnoAndEstado(idAlumno, EstadoFalta.SIN_JUSTIFICAR);
        long enRevision = faltaRepository.countByAlumno_IdAlumnoAndEstado(idAlumno, EstadoFalta.EN_REVISION);
        long justificadas = faltaRepository.countByAlumno_IdAlumnoAndEstado(idAlumno, EstadoFalta.JUSTIFICADA);

        return new ResumenFaltasResponseDto(idAlumno, sinJustificar + enRevision + justificadas,
                sinJustificar, enRevision, justificadas);
    }

    private void comprobarAlumno(Long idAlumno) {
        if (!alumnoRepository.existsById(idAlumno)) {
            throw new AlumnoNotFoundException("Alumno no encontrado con el ID: " + idAlumno);
        }
    }

    // Método auxiliar de mapeo de Entidad a DTO
    private FaltaResponseDto mapToResponseDto(Falta falta) {
        return new FaltaResponseDto(
            falta.getIdFalta(),
            falta.getAlumno().getIdAlumno(),
            falta.getFecha(),
            falta.getNumeroSesion(),
            falta.getModulo(),
            falta.getEstado()
        );
    }

}
