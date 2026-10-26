package edu.altair.apicrm.services.impl;

import edu.altair.apicrm.dtos.MatriculaRequestDto;
import edu.altair.apicrm.dtos.MatriculaResponseDto;
import edu.altair.apicrm.dtos.NotaRequestDto;
import edu.altair.apicrm.entities.Asignatura;
import edu.altair.apicrm.entities.Estudiante;
import edu.altair.apicrm.entities.Matricula;
import edu.altair.apicrm.exceptions.AsignaturaNotFoundException;
import edu.altair.apicrm.exceptions.ConflictoDatosException;
import edu.altair.apicrm.exceptions.EstudianteNotFoundException;
import edu.altair.apicrm.exceptions.MatriculaNotFoundException;
import edu.altair.apicrm.exceptions.SolicitudInvalidaException;
import edu.altair.apicrm.repositories.AsignaturaRepository;
import edu.altair.apicrm.repositories.EstudianteRepository;
import edu.altair.apicrm.repositories.MatriculaRepository;
import edu.altair.apicrm.services.MatriculaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static edu.altair.apicrm.services.impl.Validaciones.idObligatorio;

@Service
public class MatriculaServiceImpl implements MatriculaService {

    private static final BigDecimal NOTA_MINIMA = BigDecimal.ZERO;
    private static final BigDecimal NOTA_MAXIMA = BigDecimal.TEN;
    private static final int DECIMALES_NOTA = 2;

    private final MatriculaRepository matriculaRepository;
    private final EstudianteRepository estudianteRepository;
    private final AsignaturaRepository asignaturaRepository;

    // Inyección de dependencias por constructor
    public MatriculaServiceImpl(MatriculaRepository matriculaRepository,
                                EstudianteRepository estudianteRepository,
                                AsignaturaRepository asignaturaRepository) {
        this.matriculaRepository = matriculaRepository;
        this.estudianteRepository = estudianteRepository;
        this.asignaturaRepository = asignaturaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MatriculaResponseDto> obtenerTodas(Long idEstudiante, Long idAsignatura) {
        if (idEstudiante != null && !estudianteRepository.existsById(idEstudiante)) {
            throw new EstudianteNotFoundException("Estudiante no encontrado con el ID: " + idEstudiante);
        }
        if (idAsignatura != null && !asignaturaRepository.existsById(idAsignatura)) {
            throw new AsignaturaNotFoundException("Asignatura no encontrada con el ID: " + idAsignatura);
        }

        List<Matricula> matriculas;
        if (idEstudiante == null && idAsignatura == null) {
            matriculas = matriculaRepository.findAll();
        } else if (idAsignatura == null) {
            matriculas = matriculaRepository.findByEstudiante_IdEstudianteOrderByFechaMatriculacionDesc(idEstudiante);
        } else if (idEstudiante == null) {
            matriculas = matriculaRepository.findByAsignatura_IdAsignaturaOrderByFechaMatriculacionDesc(idAsignatura);
        } else {
            // Con los dos filtros, como mucho hay una matrícula (restricción única estudiante + asignatura)
            matriculas = matriculaRepository.findByEstudiante_IdEstudianteOrderByFechaMatriculacionDesc(idEstudiante)
                    .stream()
                    .filter(m -> m.getAsignatura().getIdAsignatura().equals(idAsignatura))
                    .toList();
        }

        return matriculas.stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MatriculaResponseDto obtenerPorId(Long id) {
        return mapToResponseDto(buscarMatricula(id));
    }

    @Override
    @Transactional
    public MatriculaResponseDto matricular(MatriculaRequestDto dto) {
        // 1. Validación de los datos recibidos
        idObligatorio("idEstudiante", dto.idEstudiante());
        idObligatorio("idAsignatura", dto.idAsignatura());
        LocalDate fecha = (dto.fechaMatriculacion() == null) ? LocalDate.now() : dto.fechaMatriculacion();
        if (fecha.isAfter(LocalDate.now())) {
            throw new SolicitudInvalidaException("La fecha de matriculación no puede ser futura.");
        }

        // 2. El estudiante y la asignatura deben existir
        Estudiante estudiante = estudianteRepository.findById(dto.idEstudiante())
                .orElseThrow(() -> new EstudianteNotFoundException(
                        "Estudiante no encontrado con el ID: " + dto.idEstudiante()));
        Asignatura asignatura = asignaturaRepository.findById(dto.idAsignatura())
                .orElseThrow(() -> new AsignaturaNotFoundException(
                        "Asignatura no encontrada con el ID: " + dto.idAsignatura()));

        // 3. Un estudiante solo puede matricularse una vez en cada asignatura
        if (matriculaRepository.existsByEstudiante_IdEstudianteAndAsignatura_IdAsignatura(
                dto.idEstudiante(), dto.idAsignatura())) {
            throw new ConflictoDatosException("El estudiante " + dto.idEstudiante()
                    + " ya está matriculado en la asignatura " + dto.idAsignatura() + ".");
        }

        // 4. Se guarda la matrícula (sin nota) y la asignatura pasa a estar activa
        Matricula guardada = matriculaRepository.save(new Matricula(estudiante, asignatura, fecha, null));
        if (!asignatura.isActiva()) {
            asignatura.setActiva(true);
            asignaturaRepository.save(asignatura);
        }

        return mapToResponseDto(guardada);
    }

    @Override
    @Transactional
    public MatriculaResponseDto calificar(Long id, NotaRequestDto dto) {
        Matricula matricula = buscarMatricula(id);
        BigDecimal nota = dto.notaFinal();

        if (nota != null) {
            if (nota.compareTo(NOTA_MINIMA) < 0 || nota.compareTo(NOTA_MAXIMA) > 0) {
                throw new SolicitudInvalidaException("La nota final debe estar entre 0 y 10.");
            }
            if (nota.stripTrailingZeros().scale() > DECIMALES_NOTA) {
                throw new SolicitudInvalidaException(
                        "La nota final no puede tener más de " + DECIMALES_NOTA + " decimales.");
            }
            nota = nota.setScale(DECIMALES_NOTA);
        }

        matricula.setNotaFinal(nota);
        return mapToResponseDto(matriculaRepository.save(matricula));
    }

    @Override
    @Transactional
    public void anular(Long id) {
        Matricula matricula = buscarMatricula(id);
        Asignatura asignatura = matricula.getAsignatura();

        // Matrículas que quedarán en la asignatura una vez anulada esta
        long restantes = matriculaRepository.countByAsignatura_IdAsignatura(asignatura.getIdAsignatura()) - 1;

        matriculaRepository.delete(matricula);

        // Sin ningún matriculado la asignatura deja de estar activa
        if (restantes <= 0 && asignatura.isActiva()) {
            asignatura.setActiva(false);
            asignaturaRepository.save(asignatura);
        }
    }

    private Matricula buscarMatricula(Long id) {
        return matriculaRepository.findById(id)
                .orElseThrow(() -> new MatriculaNotFoundException("Matrícula no encontrada con el ID: " + id));
    }

    // Método auxiliar de mapeo de Entidad a DTO
    private MatriculaResponseDto mapToResponseDto(Matricula matricula) {
        Estudiante estudiante = matricula.getEstudiante();
        Asignatura asignatura = matricula.getAsignatura();
        return new MatriculaResponseDto(
            matricula.getIdMatricula(),
            estudiante.getIdEstudiante(),
            estudiante.getNombre(),
            asignatura.getIdAsignatura(),
            asignatura.getNombre(),
            matricula.getFechaMatriculacion(),
            matricula.getNotaFinal()
        );
    }

}
