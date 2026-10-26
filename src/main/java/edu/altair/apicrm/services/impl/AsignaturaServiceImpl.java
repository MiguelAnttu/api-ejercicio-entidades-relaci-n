package edu.altair.apicrm.services.impl;

import edu.altair.apicrm.dtos.AsignaturaRequestDto;
import edu.altair.apicrm.dtos.AsignaturaResponseDto;
import edu.altair.apicrm.entities.Asignatura;
import edu.altair.apicrm.entities.Profesor;
import edu.altair.apicrm.exceptions.AsignaturaNotFoundException;
import edu.altair.apicrm.exceptions.ConflictoDatosException;
import edu.altair.apicrm.exceptions.ProfesorNotFoundException;
import edu.altair.apicrm.repositories.AsignaturaRepository;
import edu.altair.apicrm.repositories.MatriculaRepository;
import edu.altair.apicrm.repositories.ProfesorRepository;
import edu.altair.apicrm.services.AsignaturaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static edu.altair.apicrm.services.impl.Validaciones.idObligatorio;
import static edu.altair.apicrm.services.impl.Validaciones.textoObligatorio;

@Service
public class AsignaturaServiceImpl implements AsignaturaService {

    private static final int LONGITUD_MAXIMA_CODIGO = 10;
    private static final int LONGITUD_MAXIMA_NOMBRE = 100;

    private final AsignaturaRepository asignaturaRepository;
    private final ProfesorRepository profesorRepository;
    private final MatriculaRepository matriculaRepository;

    // Inyección de dependencias por constructor
    public AsignaturaServiceImpl(AsignaturaRepository asignaturaRepository,
                                 ProfesorRepository profesorRepository,
                                 MatriculaRepository matriculaRepository) {
        this.asignaturaRepository = asignaturaRepository;
        this.profesorRepository = profesorRepository;
        this.matriculaRepository = matriculaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AsignaturaResponseDto> obtenerTodas(Long idProfesor, Boolean activa) {
        if (idProfesor != null && !profesorRepository.existsById(idProfesor)) {
            throw new ProfesorNotFoundException("Profesor no encontrado con el ID: " + idProfesor);
        }

        List<Asignatura> asignaturas;
        if (idProfesor == null && activa == null) {
            asignaturas = asignaturaRepository.findAllByOrderByCodigoAsc();
        } else if (idProfesor == null) {
            asignaturas = asignaturaRepository.findByActivaOrderByCodigoAsc(activa);
        } else if (activa == null) {
            asignaturas = asignaturaRepository.findByProfesor_IdProfesorOrderByCodigoAsc(idProfesor);
        } else {
            asignaturas = asignaturaRepository.findByProfesor_IdProfesorAndActivaOrderByCodigoAsc(idProfesor, activa);
        }

        return asignaturas.stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AsignaturaResponseDto obtenerPorId(Long id) {
        return mapToResponseDto(buscarAsignatura(id));
    }

    @Override
    @Transactional
    public AsignaturaResponseDto crear(AsignaturaRequestDto dto) {
        String codigo = textoObligatorio("codigo", dto.codigo(), LONGITUD_MAXIMA_CODIGO);
        String nombre = textoObligatorio("nombre", dto.nombre(), LONGITUD_MAXIMA_NOMBRE);
        idObligatorio("idProfesor", dto.idProfesor());

        Profesor profesor = buscarProfesor(dto.idProfesor());
        if (asignaturaRepository.existsByCodigo(codigo)) {
            throw new ConflictoDatosException("Ya existe una asignatura con el código " + codigo + ".");
        }

        // Recién creada no tiene matriculados, así que queda inactiva (activa = false por defecto)
        Asignatura guardada = asignaturaRepository.save(new Asignatura(codigo, nombre, profesor));
        return mapToResponseDto(guardada);
    }

    @Override
    @Transactional
    public AsignaturaResponseDto actualizar(Long id, AsignaturaRequestDto dto) {
        Asignatura asignatura = buscarAsignatura(id);
        String codigo = textoObligatorio("codigo", dto.codigo(), LONGITUD_MAXIMA_CODIGO);
        String nombre = textoObligatorio("nombre", dto.nombre(), LONGITUD_MAXIMA_NOMBRE);
        idObligatorio("idProfesor", dto.idProfesor());

        Profesor profesor = buscarProfesor(dto.idProfesor());
        if (asignaturaRepository.existsByCodigoAndIdAsignaturaNot(codigo, id)) {
            throw new ConflictoDatosException("Ya existe otra asignatura con el código " + codigo + ".");
        }

        asignatura.setCodigo(codigo);
        asignatura.setNombre(nombre);
        asignatura.setProfesor(profesor);
        return mapToResponseDto(asignaturaRepository.save(asignatura));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Asignatura asignatura = buscarAsignatura(id);

        long matriculados = matriculaRepository.countByAsignatura_IdAsignatura(id);
        if (matriculados > 0) {
            throw new ConflictoDatosException("No se puede eliminar la asignatura " + id
                    + " porque tiene " + matriculados + " matrícula(s). Anúlelas antes.");
        }

        asignaturaRepository.delete(asignatura);
    }

    private Asignatura buscarAsignatura(Long id) {
        return asignaturaRepository.findById(id)
                .orElseThrow(() -> new AsignaturaNotFoundException("Asignatura no encontrada con el ID: " + id));
    }

    private Profesor buscarProfesor(Long id) {
        return profesorRepository.findById(id)
                .orElseThrow(() -> new ProfesorNotFoundException("Profesor no encontrado con el ID: " + id));
    }

    // Método auxiliar de mapeo de Entidad a DTO
    private AsignaturaResponseDto mapToResponseDto(Asignatura asignatura) {
        Profesor profesor = asignatura.getProfesor();
        Long id = asignatura.getIdAsignatura();

        // La consulta AVG devuelve null si ninguna matrícula tiene nota
        Double media = matriculaRepository.calcularNotaMedia(id);
        Double notaMedia = (media == null) ? null
                : BigDecimal.valueOf(media).setScale(2, RoundingMode.HALF_UP).doubleValue();

        return new AsignaturaResponseDto(
            id,
            asignatura.getCodigo(),
            asignatura.getNombre(),
            profesor.getIdProfesor(),
            profesor.getNombre(),
            asignatura.isActiva(),
            matriculaRepository.countByAsignatura_IdAsignatura(id),
            notaMedia
        );
    }

}
